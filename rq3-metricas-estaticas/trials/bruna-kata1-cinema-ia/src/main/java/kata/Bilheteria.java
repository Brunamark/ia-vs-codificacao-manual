package kata;

import java.util.List;


/**
 * Kata 1 — Bilheteria do Cinema.
 *
 * <p>As regras completas estão em ENUNCIADO.md, na raiz deste projeto.
 * Não altere a assinatura do método nem os arquivos de teste.
 */
public class Bilheteria {

    private static final int PRECO_2D = 3295;
    private static final int PRECO_3D = 4495;
    private static final int OCULOS_3D = 500;
    /**
     * Calcula o total arrecadado com os ingressos vendidos.
     *
     * @param ingressos linhas no formato {@code id;sessao;categoria;dia}
     * @return valor total arrecadado, em centavos
     */
    public int totalCentavos(List<String> ingressos) {
        // TODO: implementar conforme o ENUNCIADO.md
               int total = 0;

        for (String ingresso : ingressos) {
            String[] campos = ingresso.split(";");
            String sessao = campos[1];
            String categoria = campos[2];
            String dia = campos[3];

            int precoCheio = sessao.equals("3d") ? PRECO_3D : PRECO_2D;

            boolean meiaPorCategoria = categoria.equals("estudante")
                    || categoria.equals("idoso")
                    || categoria.equals("crianca");

            boolean diaDoCinema = dia.equals("ter") || dia.equals("qua");

            boolean temMeia = meiaPorCategoria || (categoria.equals("inteira") && diaDoCinema);

            int valor = temMeia ? precoCheio / 2 : precoCheio;

            if (sessao.equals("3d")) {
                valor += OCULOS_3D;
            }

            total += valor;
        }

        return total;
    }
}
