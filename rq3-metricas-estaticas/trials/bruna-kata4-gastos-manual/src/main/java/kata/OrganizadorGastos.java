package kata;

import java.util.List;

public class OrganizadorGastos {

    public int saldoCentavos(List<String> lancamentos) {
        int saldo = 0;

        for (String lancamento : lancamentos) {
            String[] campos = lancamento.split(";");
            // campos[0] = data (não usada)
            // campos[1] = categoria (não usada)
            String tipo = campos[2];
            int valor = Integer.parseInt(campos[3]);
            int parcelas = Integer.parseInt(campos[4]);

            if (valor <= 0 || parcelas < 1) {
                continue;
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