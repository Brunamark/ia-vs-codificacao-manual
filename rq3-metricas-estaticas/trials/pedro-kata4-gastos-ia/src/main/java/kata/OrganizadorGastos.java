package kata;

import java.util.List;

/**
 * Kata 4 — Organizador de Gastos.
 *
 * <p>As regras completas estão em ENUNCIADO.md, na raiz deste projeto.
 * Não altere a assinatura do método nem os arquivos de teste.
 */
public class OrganizadorGastos {

    /**
     * Calcula o saldo do mês a partir dos lançamentos financeiros.
     *
     * @param lancamentos linhas no formato {@code data;categoria;tipo;valor;parcelas}
     * @return saldo do mês em centavos, podendo ser negativo
     */
    public int saldoCentavos(List<String> lancamentos) {
        int saldo = 0;

        for (String lancamento : lancamentos) {
            String[] campos = lancamento.split(";");

            String tipo = campos[2];
            int valor = Integer.parseInt(campos[3]);
            int parcelas = Integer.parseInt(campos[4]);

            if (valor <= 0 || parcelas < 1) {
                continue; // lançamento inválido, descarta
            }

            if (tipo.equals("receita")) {
                saldo += valor;
            } else if (tipo.equals("despesa")) {
                int parcelaDoMes = dividirArredondandoParaCima(valor, parcelas);
                saldo -= parcelaDoMes;
            }
        }

        return saldo;
    }

    private int dividirArredondandoParaCima(int valor, int parcelas) {
        return (valor + parcelas - 1) / parcelas;
    }
}
