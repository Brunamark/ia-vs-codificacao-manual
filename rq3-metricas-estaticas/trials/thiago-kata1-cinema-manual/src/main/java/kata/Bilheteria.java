package kata;

import java.util.List;

/**
 * Kata 1 — Bilheteria do Cinema (SOLUÇÃO DE REFERÊNCIA).
 *
 * <p>Este arquivo NÃO faz parte do esqueleto entregue ao participante: ele existe apenas para
 * o check_katas.py validar que os testes de aceitação são satisfazíveis. Não abra esta pasta
 * durante um trial.
 */
public class Bilheteria {

    private static final int PRECO_2D = 3295;
    private static final int PRECO_3D = 4495;
    private static final int VALOR_OCULOS = 500;

    public int totalCentavos(List<String> ingressos) {
        int total = 0;
        for (String ingresso : ingressos) {
            total += valorDoIngresso(ingresso);
        }
        return total;
    }

    private int valorDoIngresso(String ingresso) {
        String[] campos = ingresso.split(";");
        String sessao = campos[1];
        String categoria = campos[2];
        String dia = campos[3];

        int precoCheio = sessao.equals("3d") ? PRECO_3D : PRECO_2D;
        int valor = temDireitoAMeia(categoria, dia) ? precoCheio / 2 : precoCheio;

        if (sessao.equals("3d")) {
            valor += VALOR_OCULOS;
        }
        return valor;
    }

    /**
     * A meia-entrada não é cumulativa: ter direito por categoria ou por ser dia do cinema
     * produz o mesmo desconto de 50%.
     */
    private boolean temDireitoAMeia(String categoria, String dia) {
        return !categoria.equals("inteira") || dia.equals("ter") || dia.equals("qua");
    }
}
