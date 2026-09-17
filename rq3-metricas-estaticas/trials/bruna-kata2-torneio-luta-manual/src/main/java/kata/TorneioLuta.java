package kata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TorneioLuta {

    private static class Stats {
        String nome;
        int pontos = 0;
        int saldo = 0;
        int perfect = 0;

        Stats(String nome) {
            this.nome = nome;
        }
    }

    public List<String> classificacao(List<String> lutas) {
        Map<String, Stats> tabela = new LinkedHashMap<>();

        for (String luta : lutas) {
            String[] campos = luta.split(";");
            String lutadorA = campos[0];
            int roundsA = Integer.parseInt(campos[1]);
            int roundsB = Integer.parseInt(campos[2]);
            String lutadorB = campos[3];

            Stats statsA = tabela.computeIfAbsent(lutadorA, Stats::new);
            Stats statsB = tabela.computeIfAbsent(lutadorB, Stats::new);

            statsA.saldo += roundsA - roundsB;
            statsB.saldo += roundsB - roundsA;

            if (roundsA == roundsB) {
                // double KO
                statsA.pontos += 1;
                statsB.pontos += 1;
            } else if (roundsA > roundsB) {
                statsA.pontos += 3;
                if (roundsB == 0) {
                    statsA.perfect += 1;
                }
            } else {
                statsB.pontos += 3;
                if (roundsA == 0) {
                    statsB.perfect += 1;
                }
            }
        }

        List<Stats> lista = new ArrayList<>(tabela.values());

        lista.sort(
            Comparator.<Stats>comparingInt(s -> s.pontos).reversed()
                .thenComparing(Comparator.<Stats>comparingInt(s -> s.saldo).reversed())
                .thenComparing(Comparator.<Stats>comparingInt(s -> s.perfect).reversed())
                .thenComparing(s -> s.nome)
        );

        List<String> resultado = new ArrayList<>();
        for (Stats s : lista) {
            resultado.add(s.nome + ";" + s.pontos + ";" + s.saldo);
        }

        return resultado;
    }
}