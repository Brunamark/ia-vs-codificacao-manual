package kata;

import java.util.List;

/**
 * Kata 4 — Organizador de Gastos (SOLUÇÃO DE REFERÊNCIA).
 *
 * <p>Este arquivo NÃO faz parte do esqueleto entregue ao participante: ele existe apenas para
 * o check_katas.py validar que os testes de aceitação são satisfazíveis. Não abra esta pasta
 * durante um trial.
 */
public class OrganizadorGastos {

    public int saldoCentavos(List<String> lancamentos) {
        int saldo = 0;
        for (String lancamento : lancamentos) {
            saldo += efeitoNoSaldo(lancamento);
        }
        return saldo;
    }

    private int efeitoNoSaldo(String lancamento) {
        String[] campos = lancamento.split(";");
        String tipo = campos[2];
        int valor = Integer.parseInt(campos[3]);
        int parcelas = Integer.parseInt(campos[4]);

        if (valor <= 0 || parcelas < 1) {
            return 0;
        }
        if (tipo.equals("receita")) {
            return valor; // o campo parcelas é ignorado em receitas
        }
        return -parcelaDoMes(valor, parcelas);
    }

    /** Divisão inteira arredondada para cima, sem usar ponto flutuante. */
    private int parcelaDoMes(int valor, int parcelas) {
        return (valor + parcelas - 1) / parcelas;
    }
}
