package kata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de aceitação do Kata 4 — Organizador de Gastos.
 *
 * <p>ARQUIVO IMUTÁVEL: alterar qualquer teste invalida o trial.
 */
class OrganizadorGastosTest {

    private final OrganizadorGastos organizador = new OrganizadorGastos();

    @Test
    @DisplayName("sem lancamentos o saldo e zero")
    void semLancamentosOSaldoEZero() {
        assertEquals(0, organizador.saldoCentavos(List.of()));
    }

    @Test
    @DisplayName("receita soma o valor integral")
    void receitaSomaOValorIntegral() {
        assertEquals(500000, organizador.saldoCentavos(List.of("2026-09-01;salario;receita;500000;1")));
    }

    @Test
    @DisplayName("despesa a vista subtrai o valor integral")
    void despesaAVistaSubtraiOValorIntegral() {
        assertEquals(-15000, organizador.saldoCentavos(List.of("2026-09-01;mercado;despesa;15000;1")));
    }

    @Test
    @DisplayName("receita e despesa se compensam no saldo")
    void receitaEDespesaSeCompensam() {
        List<String> lancamentos = List.of(
                "2026-09-01;salario;receita;500000;1",
                "2026-09-01;mercado;despesa;15000;1");
        assertEquals(485000, organizador.saldoCentavos(lancamentos));
    }

    @Test
    @DisplayName("despesa parcelada exata desconta apenas uma parcela")
    void despesaParceladaExataDescontaUmaParcela() {
        assertEquals(-10000, organizador.saldoCentavos(List.of("2026-09-05;eletro;despesa;120000;12")));
    }

    @Test
    @DisplayName("parcela com divisao inexata arredonda para cima")
    void parcelaComDivisaoInexataArredondaParaCima() {
        assertEquals(-3334, organizador.saldoCentavos(List.of("2026-09-05;curso;despesa;10000;3")));
    }

    @Test
    @DisplayName("campo parcelas e ignorado quando o tipo e receita")
    void parcelasEIgnoradoEmReceita() {
        assertEquals(90000, organizador.saldoCentavos(List.of("2026-09-01;bonus;receita;90000;3")));
    }

    @Test
    @DisplayName("lancamento com valor nao positivo e descartado")
    void lancamentoComValorNaoPositivoEDescartado() {
        List<String> lancamentos = List.of(
                "2026-09-01;erro;despesa;0;1",
                "2026-09-01;estorno;receita;-500;1",
                "2026-09-02;pix;receita;10000;1");
        assertEquals(10000, organizador.saldoCentavos(lancamentos));
    }

    @Test
    @DisplayName("lancamento com parcelas menor que um e descartado")
    void lancamentoComParcelasMenorQueUmEDescartado() {
        List<String> lancamentos = List.of(
                "2026-09-01;erro;despesa;9000;0",
                "2026-09-02;farmacia;despesa;9000;1");
        assertEquals(-9000, organizador.saldoCentavos(lancamentos));
    }

    @Test
    @DisplayName("mes completo pode fechar com saldo negativo")
    void mesCompletoPodeFecharComSaldoNegativo() {
        List<String> lancamentos = List.of(
                "2026-09-01;salario;receita;300000;1",
                "2026-09-02;aluguel;despesa;180000;1",
                "2026-09-03;notebook;despesa;700000;10",
                "2026-09-04;curso;despesa;10000;3",
                "2026-09-05;freela;receita;50000;2",
                "2026-09-06;mercado;despesa;125000;1");
        assertEquals(-28334, organizador.saldoCentavos(lancamentos));
    }
}
