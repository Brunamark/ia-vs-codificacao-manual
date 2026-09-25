"""
Card 3 — RQ3: complexidade total vs. decomposição.

Achado central: a complexidade total (WMC) é praticamente igual entre os
tratamentos (7 a 8), mas a distribuição dela não. Dispersão com WMC total no
eixo X, nº de métodos no Y e tamanho do ponto proporcional aos métodos privados.

Dados: output/metricas-estruturais.csv, transcrito da tabela da seção 6.3 do relatório.

Uso isolado: python analise-final/scripts/cards/card3_rq3_complexidade.py
"""

import sys
from collections import defaultdict
from pathlib import Path

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from matplotlib.lines import Line2D  # noqa: E402
from matplotlib.ticker import FixedLocator, MaxNLocator  # noqa: E402

from cards import comum  # noqa: E402

ESTRUTURAIS_CSV = comum.OUTPUT / "metricas-estruturais.csv"
TAMANHO_BASE = 90       # área mínima: os trials com 0 métodos privados continuam visíveis
TAMANHO_POR_PRIVADO = 160
DESLOCAMENTO = 0.07     # afastamento horizontal, só visual, de pontos coincidentes


def tamanho(privados: int) -> float:
    return TAMANHO_BASE + TAMANHO_POR_PRIVADO * privados


def grafico_rq3() -> Path:
    trials = []
    for linha in comum.ler_csv(ESTRUTURAIS_CSV):
        kata, tratamento = comum.parse_alvo(linha["alvo"])
        trials.append({
            "kata": kata,
            "tratamento": tratamento,
            "wmc_total": float(linha["wmc_total"]),
            "metodos": int(linha["metodos"]),
            "privados": int(linha["metodos_privados"]),
        })
    trials.sort(key=lambda t: comum.chave_ordem(t["tratamento"], t["kata"]))

    # Pontos com as mesmas coordenadas (K1 e K4) são afastados lateralmente para
    # que os quatro trials apareçam; o valor real continua no rótulo e na nota.
    grupos = defaultdict(list)
    for t in trials:
        grupos[(t["wmc_total"], t["metodos"])].append(t)
    coincidentes = [g for g in grupos.values() if len(g) > 1]
    for grupo in grupos.values():
        centro = (len(grupo) - 1) / 2
        for i, t in enumerate(grupo):
            t["x"] = t["wmc_total"] + (i - centro) * 2 * DESLOCAMENTO

    fig, ax = comum.nova_figura(9, 5)

    for t in trials:
        ax.scatter(t["x"], t["metodos"], s=tamanho(t["privados"]),
                   color=comum.CORES[t["tratamento"]], edgecolors=comum.SUPERFICIE,
                   linewidths=2, zorder=3)
        lado = -1 if t["x"] < t["wmc_total"] else 1
        ax.annotate(comum.KATAS[t["kata"]], (t["x"], t["metodos"]),
                    xytext=(lado * 16, 12), textcoords="offset points",
                    ha="right" if lado < 0 else "left", va="bottom",
                    fontsize=9.5, color=comum.TEXTO)

    ax.set_xlim(6.5, 8.5)
    ax.xaxis.set_major_locator(FixedLocator([7, 8]))
    ax.set_xlabel("WMC total da classe (faixa estreita: 7 a 8)",
                  color=comum.TEXTO_SECUNDARIO, fontsize=9.5)
    ax.set_ylim(0, 4)
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax.set_ylabel("nº de métodos", color=comum.TEXTO_SECUNDARIO, fontsize=9.5)
    ax.grid(color=comum.GRADE, linewidth=0.8)

    cores = [Line2D([], [], marker="o", linestyle="", markersize=9,
                    color=comum.CORES[t], label=comum.ROTULO_TRATAMENTO[t])
             for t in comum.ORDEM_TRATAMENTO]
    legenda_cor = ax.legend(handles=cores, title="tratamento", loc="upper right",
                            frameon=False, fontsize=9, title_fontsize=9)
    ax.add_artist(legenda_cor)
    valores_privados = sorted({t["privados"] for t in trials})
    tamanhos = [ax.scatter([], [], s=tamanho(p), color=comum.TEXTO_SECUNDARIO, alpha=0.45,
                           label=str(p)) for p in valores_privados]
    ax.legend(handles=tamanhos, title="métodos privados", loc="lower left",
              frameon=False, fontsize=9, title_fontsize=9, labelspacing=1.2, borderpad=0.8)

    comum.titulo(ax, "RQ3 · Complexidade total vs. decomposição",
                 "A complexidade total quase não muda; muda como ela é distribuída em métodos.")
    if coincidentes:
        nomes = " e ".join(comum.KATAS[t["kata"]] for t in coincidentes[0])
        g = coincidentes[0][0]
        fig.text(0.01, -0.04,
                 f"Nota: {nomes} coincidem (WMC {g['wmc_total']:.0f}, {g['metodos']} métodos, "
                 f"{g['privados']} privados); afastados lateralmente só para ficarem visíveis.",
                 fontsize=9.5, style="italic", color=comum.TEXTO_SECUNDARIO,
                 transform=fig.transFigure)

    return comum.salvar(fig, "rq3-complexidade-decomposicao.png")


if __name__ == "__main__":
    print(grafico_rq3())
