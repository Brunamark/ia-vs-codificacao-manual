package kata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de aceitação do Kata 1 — Bilheteria do Cinema.
 *
 * <p>ARQUIVO IMUTÁVEL: alterar qualquer teste invalida o trial.
 */
class BilheteriaTest {

    private final Bilheteria bilheteria = new Bilheteria();

    @Test
    @DisplayName("lista vazia arrecada zero")
    void listaVaziaArrecadaZero() {
        assertEquals(0, bilheteria.totalCentavos(List.of()));
    }

    @Test
    @DisplayName("ingresso inteira 2D custa o preco cheio")
    void ingressoInteira2dCustaPrecoCheio() {
        assertEquals(3295, bilheteria.totalCentavos(List.of("1;2d;inteira;seg")));
    }

    @Test
    @DisplayName("meia-entrada 2D arredonda para baixo")
    void meiaEntrada2dArredondaParaBaixo() {
        assertEquals(1647, bilheteria.totalCentavos(List.of("1;2d;estudante;seg")));
    }

    @Test
    @DisplayName("ingresso inteira 3D soma os oculos")
    void ingressoInteira3dSomaOsOculos() {
        assertEquals(4995, bilheteria.totalCentavos(List.of("1;3d;inteira;seg")));
    }

    @Test
    @DisplayName("meia-entrada 3D desconta so a sessao e soma os oculos integrais")
    void meiaEntrada3dSomaOculosIntegrais() {
        assertEquals(2747, bilheteria.totalCentavos(List.of("1;3d;idoso;seg")));
    }

    @Test
    @DisplayName("dia do cinema transforma inteira em meia")
    void diaDoCinemaTransformaInteiraEmMeia() {
        assertEquals(1647, bilheteria.totalCentavos(List.of("1;2d;inteira;ter")));
    }

    @Test
    @DisplayName("meia-entrada nao acumula com o dia do cinema")
    void meiaEntradaNaoAcumulaComDiaDoCinema() {
        assertEquals(1647, bilheteria.totalCentavos(List.of("1;2d;estudante;ter")));
    }

    @Test
    @DisplayName("meia nao acumula em 3D e os oculos ficam fora do desconto")
    void meiaNaoAcumulaEm3dComOculosForaDoDesconto() {
        assertEquals(2747, bilheteria.totalCentavos(List.of("1;3d;crianca;qua")));
    }

    @Test
    @DisplayName("oculos 3D sao cobrados por ingresso")
    void oculos3dSaoCobradosPorIngresso() {
        assertEquals(9990, bilheteria.totalCentavos(List.of("1;3d;inteira;seg", "2;3d;inteira;seg")));
    }

    @Test
    @DisplayName("venda com sessoes, categorias e dias variados")
    void vendaComSessoesCategoriasEDiasVariados() {
        List<String> ingressos = List.of(
                "1;2d;inteira;seg",
                "2;3d;estudante;qui",
                "3;2d;crianca;ter",
                "4;3d;inteira;qua");
        assertEquals(10436, bilheteria.totalCentavos(ingressos));
    }
}
