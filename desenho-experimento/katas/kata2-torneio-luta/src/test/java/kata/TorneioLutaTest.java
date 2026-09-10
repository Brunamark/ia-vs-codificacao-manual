package kata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de aceitação do Kata 2 — Torneio de Jogos de Luta.
 *
 * <p>ARQUIVO IMUTÁVEL: alterar qualquer teste invalida o trial.
 */
class TorneioLutaTest {

    private final TorneioLuta torneio = new TorneioLuta();

    @Test
    @DisplayName("torneio sem lutas gera classificacao vazia")
    void torneioSemLutasGeraClassificacaoVazia() {
        assertEquals(List.of(), torneio.classificacao(List.of()));
    }

    @Test
    @DisplayName("vitoria vale 3 pontos e derrota vale zero")
    void vitoriaVale3PontosEDerrotaVzero() {
        assertEquals(
                List.of("Ryu;3;2", "Ken;0;-2"),
                torneio.classificacao(List.of("Ryu;2;0;Ken")));
    }

    @Test
    @DisplayName("double KO da 1 ponto para cada lutador")
    void doubleKoDaUmPontoParaCadaLutador() {
        assertEquals(
                List.of("Ken;1;0", "Ryu;1;0"),
                torneio.classificacao(List.of("Ryu;1;1;Ken")));
    }

    @Test
    @DisplayName("double KO com placar diferente de zero tambem vale 1 ponto")
    void doubleKoComPlacarDiferenteDeZeroTambemValeUmPonto() {
        assertEquals(
                List.of("Ken;1;0", "Ryu;1;0"),
                torneio.classificacao(List.of("Ryu;2;2;Ken")));
    }

    @Test
    @DisplayName("empate em pontos desempata pelo saldo de rounds")
    void empateEmPontosDesempataPeloSaldo() {
        List<String> lutas = List.of("Ryu;2;0;Ken", "Chun-Li;2;1;Ken");
        assertEquals(
                List.of("Ryu;3;2", "Chun-Li;3;1", "Ken;0;-3"),
                torneio.classificacao(lutas));
    }

    @Test
    @DisplayName("empate em pontos e saldo desempata pelas vitorias perfect")
    void empateEmPontosESaldoDesempataPelasVitoriasPerfect() {
        List<String> lutas = List.of("Ryu;2;0;Ken", "Akuma;3;1;Guile");
        assertEquals(
                List.of("Ryu;3;2", "Akuma;3;2", "Guile;0;-2", "Ken;0;-2"),
                torneio.classificacao(lutas));
    }

    @Test
    @DisplayName("perfect e definido pelo adversario com zero rounds, nao pelo placar 2x0")
    void perfectEDefinidoPeloAdversarioComZeroRounds() {
        List<String> lutas = List.of("Ryu;3;0;Ken", "Akuma;2;0;Guile");
        assertEquals(
                List.of("Ryu;3;3", "Akuma;3;2", "Guile;0;-2", "Ken;0;-3"),
                torneio.classificacao(lutas));
    }

    @Test
    @DisplayName("empate total desempata pela ordem alfabetica")
    void empateTotalDesempataPelaOrdemAlfabetica() {
        List<String> lutas = List.of("Ryu;1;1;Ken", "Akuma;1;1;Guile");
        assertEquals(
                List.of("Akuma;1;0", "Guile;1;0", "Ken;1;0", "Ryu;1;0"),
                torneio.classificacao(lutas));
    }

    @Test
    @DisplayName("pontos e saldo acumulam entre varias lutas do mesmo lutador")
    void pontosESaldoAcumulamEntreVariasLutas() {
        List<String> lutas = List.of("Ryu;2;0;Ken", "Ken;2;1;Guile", "Guile;0;2;Ryu");
        assertEquals(
                List.of("Ryu;6;4", "Ken;3;-1", "Guile;0;-3"),
                torneio.classificacao(lutas));
    }

    @Test
    @DisplayName("torneio completo aplica todos os criterios de desempate")
    void torneioCompletoAplicaTodosOsCriterios() {
        List<String> lutas = List.of(
                "Ryu;2;0;Ken",
                "Chun-Li;2;1;Guile",
                "Ryu;1;2;Chun-Li",
                "Ken;2;2;Guile",
                "Akuma;2;0;Ryu");
        assertEquals(
                List.of("Chun-Li;6;2", "Akuma;3;2", "Ryu;3;-1", "Guile;1;-1", "Ken;1;-2"),
                torneio.classificacao(lutas));
    }
}
