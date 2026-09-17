package kata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de aceitação do Kata 3 — Contador de Calorias.
 *
 * <p>ARQUIVO IMUTÁVEL: alterar qualquer teste invalida o trial.
 */
class ContadorCaloriasTest {

    private final ContadorCalorias contador = new ContadorCalorias();

    @Test
    @DisplayName("sem registros nao ha dia acima da meta")
    void semRegistrosNaoHaDiaAcimaDaMeta() {
        assertEquals(List.of(), contador.diasAcimaDaMeta(List.of()));
    }

    @Test
    @DisplayName("dia acima da meta entra no resultado")
    void diaAcimaDaMetaEntraNoResultado() {
        assertEquals(
                List.of("2026-09-10;2500"),
                contador.diasAcimaDaMeta(List.of("2026-09-10;refeicao;2500;1")));
    }

    @Test
    @DisplayName("dia abaixo da meta nao entra no resultado")
    void diaAbaixoDaMetaNaoEntraNoResultado() {
        assertEquals(
                List.of(),
                contador.diasAcimaDaMeta(List.of("2026-09-10;refeicao;1500;1")));
    }

    @Test
    @DisplayName("dia exatamente na meta nao entra: o criterio e estritamente acima")
    void diaExatamenteNaMetaNaoEntra() {
        assertEquals(
                List.of(),
                contador.diasAcimaDaMeta(List.of("2026-09-10;refeicao;2000;1")));
    }

    @Test
    @DisplayName("quantidade multiplica as calorias do registro")
    void quantidadeMultiplicaAsCalorias() {
        assertEquals(
                List.of("2026-09-10;2400"),
                contador.diasAcimaDaMeta(List.of("2026-09-10;refeicao;800;3")));
    }

    @Test
    @DisplayName("treino desconta do total do dia")
    void treinoDescontaDoTotalDoDia() {
        List<String> registros = List.of(
                "2026-09-10;refeicao;2500;1",
                "2026-09-10;treino;600;1");
        assertEquals(List.of(), contador.diasAcimaDaMeta(registros));
    }

    @Test
    @DisplayName("dia continua acima da meta mesmo apos o desconto do treino")
    void diaContinuaAcimaDaMetaAposDescontoDoTreino() {
        List<String> registros = List.of(
                "2026-09-10;refeicao;3000;1",
                "2026-09-10;treino;400;2");
        assertEquals(List.of("2026-09-10;2200"), contador.diasAcimaDaMeta(registros));
    }

    @Test
    @DisplayName("linha exatamente igual conta uma unica vez")
    void linhaExatamenteIgualContaUmaUnicaVez() {
        List<String> registros = List.of(
                "2026-09-10;refeicao;1200;2",
                "2026-09-10;refeicao;1200;2");
        assertEquals(List.of("2026-09-10;2400"), contador.diasAcimaDaMeta(registros));
    }

    @Test
    @DisplayName("registro com quantidade nao positiva ou valor negativo e descartado")
    void registroInvalidoEDescartado() {
        List<String> registros = List.of(
                "2026-09-10;refeicao;2500;1",
                "2026-09-10;refeicao;5000;0",
                "2026-09-10;refeicao;-100;3");
        assertEquals(List.of("2026-09-10;2500"), contador.diasAcimaDaMeta(registros));
    }

    @Test
    @DisplayName("varios dias saem em ordem cronologica crescente")
    void variosDiasSaemEmOrdemCronologica() {
        List<String> registros = List.of(
                "2026-09-12;refeicao;2600;1",
                "2026-09-10;refeicao;2100;1",
                "2026-09-11;refeicao;1000;1",
                "2026-09-11;refeicao;1500;1");
        assertEquals(
                List.of("2026-09-10;2100", "2026-09-11;2500", "2026-09-12;2600"),
                contador.diasAcimaDaMeta(registros));
    }
}
