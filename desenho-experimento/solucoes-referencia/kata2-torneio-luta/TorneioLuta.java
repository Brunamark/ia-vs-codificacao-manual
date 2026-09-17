package kata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kata 2 — Torneio de Jogos de Luta (SOLUÇÃO DE REFERÊNCIA).
 *
 * <p>Este arquivo NÃO faz parte do esqueleto entregue ao participante: ele existe apenas para
 * o check_katas.py validar que os testes de aceitação são satisfazíveis. Não abra esta pasta
 * durante um trial.
 */
public class TorneioLuta {

    private static class Lutador {
        final String nome;
        int pontos;
        int saldo;
        int perfects;

        Lutador(String nome) {
            this.nome = nome;
        }
    }

    private static final Comparator<Lutador> CLASSIFICACAO =
            Comparator.comparingInt((Lutador l) -> l.pontos)
                    .thenComparingInt(l -> l.saldo)
                    .thenComparingInt(l -> l.perfects)
                    .reversed()
                    .thenComparing(l -> l.nome);

    public List<String> classificacao(List<String> lutas) {
        Map<String, Lutador> tabela = new LinkedHashMap<>();
        for (String luta : lutas) {
            registrar(tabela, luta);
        }
        List<Lutador> ordenados = new ArrayList<>(tabela.values());
        ordenados.sort(CLASSIFICACAO);

        List<String> resultado = new ArrayList<>();
        for (Lutador l : ordenados) {
            resultado.add(l.nome + ";" + l.pontos + ";" + l.saldo);
        }
        return resultado;
    }

    private void registrar(Map<String, Lutador> tabela, String luta) {
        String[] campos = luta.split(";");
        int roundsA = Integer.parseInt(campos[1]);
        int roundsB = Integer.parseInt(campos[2]);
        Lutador a = tabela.computeIfAbsent(campos[0], Lutador::new);
        Lutador b = tabela.computeIfAbsent(campos[3], Lutador::new);

        a.saldo += roundsA - roundsB;
        b.saldo += roundsB - roundsA;

        if (roundsA == roundsB) {
            a.pontos += 1; // double KO: 1 ponto para cada
            b.pontos += 1;
            return;
        }
        Lutador vencedor = roundsA > roundsB ? a : b;
        vencedor.pontos += 3;
        if (Math.min(roundsA, roundsB) == 0) {
            vencedor.perfects++; // perfect: adversário sem nenhum round
        }
    }
}
