# TrialTimer — Cronômetro de Trials (Lab02 · S01)

Script de cronometragem para o experimento **Assistentes de IA vs. codificação manual**.
Registra o *time-to-green* (métrica primária da **RQ1**), a taxa de testes passando (**RQ2**)
e o número de prompts com o assistente de IA (métrica exploratória).

## Requisitos

- **JDK 11+** (sem dependências externas)
- Testes automatizados que imprimam o padrão JUnit: `Tests run: X, Failures: Y, Errors: Z`
  (Maven Surefire, Gradle e JUnit Console Launcher são compatíveis)

> Todos os comandos abaixo assumem que você está em `rq1-rq2-tempo-e-testes/`
> (o `results.csv` e os logs caem em `output/`, ao lado de `scripts/`).

## Compilação

```bash
javac scripts/TrialTimer.java
```

## Uso

```bash
java -cp scripts TrialTimer --member ana --kata kata1 --trial 1 --treatment ia --test-cmd "mvn -q test" --cwd /caminho/do/projeto
```

| Parâmetro | Obrigatório | Descrição |
|---|---|---|
| `--member` | sim | Identificador do integrante |
| `--kata` | sim | Identificador do kata (ex.: `kata1`) |
| `--trial` | sim | Número do trial |
| `--treatment` | sim | `ia` ou `manual` |
| `--test-cmd` | sim | Comando que executa os testes de aceitação |
| `--timebox` | não | Time-box em minutos (padrão **35**; só pode ser reduzido) |
| `--cwd` | não | Diretório onde os testes rodam (padrão: diretório atual) |

### Comandos durante o trial

| Tecla | Ação |
|---|---|
| `p` + Enter | Registra 1 interação/prompt com o assistente de IA |
| `s` + Enter | Mostra status (tempo, testes, prompts) |
| `q` + Enter | Aborta o trial (registra `aborted=true`; usar apenas em problema real) |

## Regras do experimento implementadas

- **Time-box fixo de 35 min** — o script recusa `--timebox` maior que 35.
- **Censura, não descarte** — trial que atinge o time-box sem ficar "green" é registrado
  como `35:00` com `censored=true`, conforme o roteiro.
- **Verificação a cada 5 s** — a execução dos testes ocupa alguns segundos; como o custo é
  idêntico nos dois tratamentos, não enviesa a comparação (registrado como observação metodológica).

## Saídas

1. **`output/results.csv`** (append — um trial por linha), com as colunas:
   `date, member, kata, trial, treatment, test_cmd, timebox_min, time_to_green_sec,
   time_to_green_mmss, censored, aborted, tests_total, tests_passing, tests_failures,
   test_success_rate, prompts` — pronto para Pandas na S03.
2. **`output/logs/<kata>_t<trial>_<tratamento>_<timestamp>.json`** — log detalhado do trial.

## Exemplos de `--test-cmd`

```bash
# Maven
java -cp scripts TrialTimer --member ana --kata kata1 --trial 1 --treatment ia --test-cmd "mvn -q test"

# Gradle
java -cp scripts TrialTimer --member ana --kata kata2 --trial 2 --treatment manual --test-cmd "gradle test --console=plain"

# JUnit Console Launcher (sem Maven/Gradle)
java -cp scripts TrialTimer --member bruno --kata kata3 --trial 1 --treatment ia     --test-cmd "java -jar junit-platform-console-standalone-1.10.0.jar execute -c com.lab.Kata3Test"
```

> **Nota:** o script detecta automaticamente o shell do sistema (sh/cmd) para executar o comando.
