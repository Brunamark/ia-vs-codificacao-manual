package kata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TorneioLuta {

    private static class Fighter {
        String nome;
        int pontos = 0;
        int saldo = 0;
        int perfects = 0;

        Fighter(String nome) {
            this.nome = nome;
        }
    }

    public List<String> classificacao(List<String> lutas) {
        Map<String, Fighter> tabela = new LinkedHashMap<>();

        for (String luta : lutas) {
            String[] campos = luta.split(";");
            String nomeA = campos[0];
            int roundsA = Integer.parseInt(campos[1]);
            int roundsB = Integer.parseInt(campos[2]);
            String nomeB = campos[3];

            Fighter a = tabela.computeIfAbsent(nomeA, Fighter::new);
            Fighter b = tabela.computeIfAbsent(nomeB, Fighter::new);

            a.saldo += roundsA - roundsB;
            b.saldo += roundsB - roundsA;

            if (roundsA > roundsB) {
                a.pontos += 3;
                if (roundsB == 0) {
                    a.perfects++;
                }
            } else if (roundsB > roundsA) {
                b.pontos += 3;
                if (roundsA == 0) {
                    b.perfects++;
                }
            } else {
                a.pontos += 1;
                b.pontos += 1;
            }
        }

        List<Fighter> lutadores = new ArrayList<>(tabela.values());

        lutadores.sort(
                Comparator.<Fighter>comparingInt(f -> -f.pontos)
                        .thenComparingInt(f -> -f.saldo)
                        .thenComparingInt(f -> -f.perfects)
                        .thenComparing(f -> f.nome)
        );

        List<String> resultado = new ArrayList<>();
        for (Fighter f : lutadores) {
            resultado.add(f.nome + ";" + f.pontos + ";" + f.saldo);
        }
        return resultado;
    }
}