/**
 * TrialTimer.java — Cronômetro de trials para o experimento de IA vs. codificação manual.
 *
 * Registra por trial:
 *   - time-to-green: tempo até passar em TODOS os testes de aceitação (métrica primária da RQ1)
 *   - censura no time-box (padrão 35 min): se o trial não ficar "green", registra 35:00 com censored=true
 *   - nº de testes executados/passando ao final (métricas da RQ2)
 *   - nº de interações/prompts com o assistente de IA (métrica exploratória)
 *
 * Requer apenas o JDK (11+). Nenhuma dependência externa.
 *
 * Compilar:  javac TrialTimer.java
 *
 * Uso típico (time-box padrão de 35 min):
 *   java TrialTimer --member ana --kata kata1 --trial 1 --treatment ia --test-cmd "mvn -q test"
 *
 * Comandos disponíveis DURANTE o trial (digite e pressione Enter):
 *   p  -> registra 1 interação/prompt com o assistente de IA
 *   s  -> mostra o status atual (tempo, testes, prompts)
 *   q  -> aborta o trial (registra aborted=true; use apenas em caso de problema real)
 *
 * Saída (relativa ao diretório de onde o comando é executado — rode a partir
 * de rq1-rq2-tempo-e-testes/ para que caia em output/, junto do restante do lab):
 *   - output/results.csv : uma linha por trial (append), pronto para Pandas na S03
 *   - output/logs/<kata>_t<trial>_<tratamento>_<timestamp>.json : log detalhado do trial
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrialTimer {

    static final int TIMEBOX_DEFAULT_MIN = 35; // time-box fixo do roteiro: só pode ser reduzido
    static final int POLL_INTERVAL_SEC = 5;
    static final String RESULTS_CSV = "output/results.csv";
    static final String LOGS_DIR = "output/logs";

    /** Padrões de saída do JUnit / Maven Surefire / Gradle: "Tests run: 10, Failures: 1, Errors: 0, Skipped: 0" */
    static final Pattern TESTS_RUN  = Pattern.compile("Tests run:\\s*(\\d+)");
    static final Pattern FAILURES   = Pattern.compile("Failures:\\s*(\\d+)");
    static final Pattern ERRORS     = Pattern.compile("Errors:\\s*(\\d+)");

    // ---------------------------------------------------------------- args

    static class Args {
        String member, kata, treatment, testCmd, cwd;
        int trial;
        int timeboxMin = TIMEBOX_DEFAULT_MIN;

        static Args parse(String[] argv) {
            Args a = new Args();
            for (int i = 0; i < argv.length; i++) {
                switch (argv[i]) {
                    case "--member":    a.member   = req(argv, ++i, "--member");    break;
                    case "--kata":      a.kata     = req(argv, ++i, "--kata");      break;
                    case "--trial":     a.trial    = Integer.parseInt(req(argv, ++i, "--trial")); break;
                    case "--treatment": a.treatment= req(argv, ++i, "--treatment"); break;
                    case "--test-cmd":  a.testCmd  = req(argv, ++i, "--test-cmd");  break;
                    case "--timebox":   a.timeboxMin = Integer.parseInt(req(argv, ++i, "--timebox")); break;
                    case "--cwd":       a.cwd      = req(argv, ++i, "--cwd");       break;
                    default: usage("Argumento desconhecido: " + argv[i]);
                }
            }
            if (a.member == null || a.kata == null || a.treatment == null || a.testCmd == null)
                usage("Obrigatórios: --member, --kata, --trial, --treatment, --test-cmd");
            if (!a.treatment.equals("ia") && !a.treatment.equals("manual"))
                usage("--treatment deve ser 'ia' ou 'manual'");
            if (a.timeboxMin > TIMEBOX_DEFAULT_MIN)
                usage("O time-box fixo é de 35 min — só pode ser reduzido, nunca aumentado.");
            return a;
        }
        static String req(String[] argv, int i, String flag) {
            if (i >= argv.length) usage("Falta valor para " + flag);
            return argv[i];
        }
        static void usage(String msg) {
            System.err.println("ERRO: " + msg + "\n");
            System.err.println("Uso: java -cp scripts TrialTimer --member NOME --kata kataX --trial N \\");
            System.err.println("                     --treatment ia|manual --test-cmd \"mvn -q test\" \\");
            System.err.println("                     [--timebox 35] [--cwd /caminho/do/projeto]");
            System.exit(1);
        }
    }

    // ------------------------------------------------------------- modelo

    /** Resultado de uma execução dos testes. */
    static class TestResult {
        boolean green;
        int run, failures, errors;
        int passing() { return run - failures - errors; }
    }

    static class Trial {
        final Args args;
        final long timeboxSec;
        final AtomicInteger prompts = new AtomicInteger();
        final AtomicBoolean finished = new AtomicBoolean(false);
        final CountDownLatch done = new CountDownLatch(1);
        volatile String lastStatus = "aguardando início";
        volatile TestResult lastResult = new TestResult();
        long startNanos;
        String finishReason = "timebox";

        Trial(Args args) {
            this.args = args;
            this.timeboxSec = args.timeboxMin * 60L;
        }

        // ------------------------------------------------------------ execução

        TestResult runTests() {
            TestResult r = new TestResult();
            List<String> shell = shellCommand(args.testCmd);
            ProcessBuilder pb = new ProcessBuilder(shell);
            if (args.cwd != null) pb.directory(new java.io.File(args.cwd));
            pb.redirectErrorStream(true);
            try {
                Process p = pb.start();
                StringBuilder out = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) out.append(line).append('\n');
                }
                p.waitFor();
                r.run     = findInt(TESTS_RUN, out);
                r.failures= findInt(FAILURES,  out);
                r.errors  = findInt(ERRORS,    out);
                r.green   = p.exitValue() == 0 && r.run > 0 && r.failures == 0 && r.errors == 0;
            } catch (IOException | InterruptedException e) {
                r.green = false; // falha de ambiente conta como "não green"
                lastStatus = "ERRO ao executar os testes: " + e.getMessage();
            }
            return r;
        }

        static int findInt(Pattern p, StringBuilder text) {
            Matcher m = p.matcher(text);
            int last = 0;
            while (m.find()) last = Integer.parseInt(m.group(1)); // pega o total agregado (última linha)
            return last;
        }

        static List<String> shellCommand(String cmd) {
            String os = System.getProperty("os.name", "").toLowerCase();
            List<String> c = new ArrayList<>();
            if (os.contains("win")) { c.add("cmd.exe"); c.add("/c"); }
            else { c.add("sh"); c.add("-c"); }
            c.add(cmd);
            return c;
        }

        // ------------------------------------------------------------ threads

        void poller() {
            while (!finished.get()) {
                double elapsed = (System.nanoTime() - startNanos) / 1e9;
                if (elapsed >= timeboxSec) { finish("timebox"); return; }
                TestResult r = runTests();
                elapsed = (System.nanoTime() - startNanos) / 1e9;
                lastResult = r;
                lastStatus = String.format("%s | testes: %d/%d passando | prompts: %d",
                        fmt(elapsed), r.passing(), r.run, prompts.get());
                if (r.green) { finish("green"); return; }
                try { Thread.sleep(POLL_INTERVAL_SEC * 1000L); }
                catch (InterruptedException ignored) { }
            }
        }

        void reader() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while (!finished.get() && (line = in.readLine()) != null) {
                    switch (line.trim().toLowerCase()) {
                        case "p":
                            int n = prompts.incrementAndGet();
                            System.out.printf("[%s] prompt registrado (total: %d)%n",
                                    fmt(elapsedSec()), n);
                            break;
                        case "s":
                            System.out.println("[status] " + lastStatus);
                            break;
                        case "q":
                            finish("aborted");
                            return;
                        default: // ignora outras entradas
                    }
                }
            } catch (IOException ignored) { }
        }

        // ------------------------------------------------------------ finalização

        double elapsedSec() { return (System.nanoTime() - startNanos) / 1e9; }

        synchronized void finish(String reason) {
            if (finished.get()) return;
            finished.set(true);
            finishReason = reason;
            double elapsed = Math.min(elapsedSec(), timeboxSec); // censura: nunca passa do time-box
            TestResult r = runTests(); // medida final (garante contagens mesmo no timeout/abort)
            lastResult = r;
            double timeToGreen = elapsed;

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            res.put("member", args.member);
            res.put("kata", args.kata);
            res.put("trial", args.trial);
            res.put("treatment", args.treatment);
            res.put("test_cmd", args.testCmd);
            res.put("timebox_min", args.timeboxMin);
            res.put("time_to_green_sec", Math.round(timeToGreen * 10) / 10.0);
            res.put("time_to_green_mmss", fmt(timeToGreen));
            res.put("censored", reason.equals("timebox"));
            res.put("aborted", reason.equals("aborted"));
            res.put("tests_total", r.run);
            res.put("tests_passing", r.passing());
            res.put("tests_failures", r.failures);
            res.put("test_success_rate", r.run > 0 ? round4((double) r.passing() / r.run) : 0.0);
            res.put("prompts", prompts.get());

            try {
                saveCsv(res);
                saveJson(res);
            } catch (IOException e) {
                System.err.println("ERRO ao salvar resultados: " + e.getMessage());
            }

            System.out.println("\n" + "=".repeat(55));
            if (reason.equals("green"))
                System.out.printf("✔ GREEN em %s — todos os testes passaram.%n", fmt(timeToGreen));
            else if (reason.equals("timebox"))
                System.out.printf("⏱ TIMEBOX atingido (%d min) — trial CENSURADO em %s.%n",
                        args.timeboxMin, fmt(timeToGreen));
            else
                System.out.printf("✖ Trial ABORTADO em %s pelo operador.%n", fmt(timeToGreen));
            System.out.printf("  Testes passando ao final: %d/%d%n", r.passing(), r.run);
            System.out.printf("  Prompts/interações com IA: %d%n", prompts.get());
            System.out.println("  Resultado salvo em " + RESULTS_CSV);
            System.out.println("=".repeat(55));
            done.countDown();
        }

        static double round4(double v) { return Math.round(v * 10000) / 10000.0; }

        void saveCsv(Map<String, Object> res) throws IOException {
            Path csv = Paths.get(RESULTS_CSV);
            Files.createDirectories(csv.getParent());
            List<String> lines = new ArrayList<>();
            if (!Files.exists(csv))
                lines.add(String.join(",", res.keySet()));
            List<String> vals = new ArrayList<>();
            for (Object v : res.values()) {
                String s = v.toString();
                vals.add(s.contains(",") ? '"' + s + '"' : s);
            }
            lines.add(String.join(",", vals));
            Files.write(csv, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }

        void saveJson(Map<String, Object> res) throws IOException {
            Path dir = Paths.get(LOGS_DIR);
            Files.createDirectories(dir);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path log = dir.resolve(String.format("%s_t%d_%s_%s.json",
                    args.kata, args.trial, args.treatment, ts));
            StringBuilder sb = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<String, Object> e : res.entrySet()) {
                sb.append(String.format("  \"%s\": %s%s%n", e.getKey(), jsonValue(e.getValue()),
                        ++i < res.size() ? "," : ""));
            }
            sb.append("}\n");
            Files.write(log, sb.toString().getBytes(StandardCharsets.UTF_8));
        }

        static String jsonValue(Object v) {
            if (v instanceof Number || v instanceof Boolean) return v.toString();
            return '"' + v.toString().replace("\"", "\\\"") + '"';
        }

        void run() throws InterruptedException {
            System.out.println("=".repeat(55));
            System.out.printf("TRIAL %d | kata: %s | tratamento: %s%n",
                    args.trial, args.kata, args.treatment);
            System.out.printf("Time-box: %d min | Testes: %s%n", args.timeboxMin, args.testCmd);
            System.out.println("Comandos durante o trial:  p = registrar prompt | s = status | q = abortar");
            System.out.println("-".repeat(55));
            System.out.print("Preparado? Pressione Enter para INICIAR o cronômetro... ");
            System.out.flush();
            try { new BufferedReader(new InputStreamReader(System.in)).readLine(); }
            catch (IOException ignored) { }

            startNanos = System.nanoTime();
            Thread t1 = new Thread(this::poller, "poller");
            Thread t2 = new Thread(this::reader, "reader");
            t1.setDaemon(true); t2.setDaemon(true);
            t1.start(); t2.start();
            done.await(); // aguarda green, timeout ou abort
        }
    }

    static String fmt(double seconds) {
        long total = (long) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] argv) throws InterruptedException {
        Args args = Args.parse(argv);
        new Trial(args).run();
    }
}
