package kata;

import java.util.List;

/**
 * Kata 1 — Bilheteria do Cinema.
 *
 * <p>As regras completas estão em ENUNCIADO.md, na raiz deste projeto.
 * Não altere a assinatura do método nem os arquivos de teste.
 */
public class Bilheteria {

    /**
     * Calcula o total arrecadado com os ingressos vendidos.
     *
     * @param ingressos linhas no formato {@code id;sessao;categoria;dia}
     * @return valor total arrecadado, em centavos
     */
    public int totalCentavos(List<String> ingressos) {
        int total = 0;

    for (String ingresso : ingressos) {
        String[] campos = ingresso.split(";");

        String sessao = campos[1];
        String categoria = campos[2];
        String dia = campos[3];

        int precoCheio = "2d".equals(sessao) ? 3295 : 4495;

        boolean meia = categoria.equals("estudante")
                || categoria.equals("idoso")
                || categoria.equals("crianca")
                || (categoria.equals("inteira")
                    && (dia.equals("ter") || dia.equals("qua")));

        int preco = meia ? precoCheio / 2 : precoCheio;

        if ("3d".equals(sessao)) {
            preco += 500;
        }

        total += preco;
    }

    return total;
    }
}
