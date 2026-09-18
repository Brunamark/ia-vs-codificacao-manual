package kata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TorneioLuta {

    public List<String> classificacao(List<String> lutas) {
        Map<String, int[]> fighters = new LinkedHashMap<>(); // [pontos, saldo, perfects]

        for (String luta : lutas) {
            String[] campos = luta.split(";");

            String nomeA = campos[0];
            int roundsA = Integer.parseInt(campos[1]);
            int roundsB = Integer.parseInt(campos[2]);
            String nomeB = campos[3];

            fighters.putIfAbsent(nomeA, new int[3]);
            fighters.putIfAbsent(nomeB, new int[3]);

            int[] a = fighters.get(nomeA);
            int[] b = fighters.get(nomeB);

            a[1] += roundsA - roundsB;
            b[1] += roundsB - roundsA;

            if (roundsA == roundsB) {
                a[0] += 1;
                b[0] += 1;
            } else if (roundsA > roundsB) {
                a[0] += 3;
                if (roundsB == 0) {
                    a[2] += 1;
                }
            } else {
                b[0] += 3;
                if (roundsA == 0) {
                    b[2] += 1;
                }
            }
        }

        List<String> nomes = new ArrayList<>(fighters.keySet());

        nomes.sort(
            Comparator.comparingInt((String n) -> fighters.get(n)[0]).reversed()
                .thenComparing(Comparator.comparingInt((String n) -> fighters.get(n)[1]).reversed())
                .thenComparing(Comparator.comparingInt((String n) -> fighters.get(n)[2]).reversed())
                .thenComparing(Comparator.naturalOrder())
        );

        List<String> resultado = new ArrayList<>();
        for (String nome : nomes) {
            int[] stats = fighters.get(nome);
            resultado.add(nome + ";" + stats[0] + ";" + stats[1]);
        }

        return resultado;
    }
}