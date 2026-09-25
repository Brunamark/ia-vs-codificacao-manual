"""
Card 1 — RQ1: tempo e iterações até o green.

Dot plot horizontal, um ponto por trial, tempo até o green em escala log e cada
ponto rotulado com o nº de prompts. A escala log é necessária: em escala linear
os dois trials com IA (~1 min) colapsam no zero diante dos manuais (~11 min).

Dados: output/tempo-iteracoes.csv, transcrito da tabela da seção 6.1 do
relatório. Nos trials manuais o relatório registra `prompts` como n/a, "zero
estrutural, por definição"; o CSV guarda esse zero.

Uso isolado: python analise-final/scripts/cards/card1_rq1_tempo_iteracoes.py
"""

import sys
from pathlib import Path

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from matplotlib.lines import Line2D  # noqa: E402
from matplotlib.ticker import FixedLocator, NullLocator  # noqa: E402

from cards import comum  # noqa: E402

TEMPO_CSV = comum.OUTPUT / "tempo-iteracoes.csv"
NOTA = "o tempo sob IA é majoritariamente transcrição, não resolução"


def rotulo_prompts(prompts: int) -> str:
    return f"{prompts} prompt" if prompts == 1 else f"{prompts} prompts"


def grafico_rq1() -> Path:
    trials = comum.ler_csv(TEMPO_CSV)
    trials.sort(key=lambda t: comum.chave_ordem(t["treatment"], t["kata"]))

    fig, ax = comum.nova_figura(9, 4.2)
    posicoes = range(len(trials))[::-1]  # primeiro trial no topo

    for y, trial in zip(posicoes, trials):
        tratamento = trial["treatment"]
        segundos = float(trial["time_to_green_sec"])
        prompts = int(trial["prompts"])
        ax.scatter(segundos, y, s=110, color=comum.CORES[tratamento],
                   edgecolors=comum.SUPERFICIE, linewidths=2, zorder=3)
        ax.annotate(f"{rotulo_prompts(prompts)} · {comum.fmt_br(segundos)} s",
                    (segundos, y), xytext=(10, 0), textcoords="offset points",
                    va="center", fontsize=9.5, color=comum.TEXTO)

    ax.set_yticks(list(posicoes))
    ax.set_yticklabels([f"T{t['trial']} · {comum.KATAS[t['kata']]}" for t in trials])
    ax.set_ylim(-0.7, len(trials) - 0.3)

    ax.set_xscale("log")
    ax.set_xlim(30, 2400)
    marcas = [30, 60, 120, 300, 600, 1200]
    ax.xaxis.set_major_locator(FixedLocator(marcas))
    ax.xaxis.set_minor_locator(NullLocator())
    ax.set_xticklabels([f"{m} s" if m < 60 else f"{m // 60} min" for m in marcas])
    ax.set_xlabel("tempo até o green (escala log)", color=comum.TEXTO_SECUNDARIO, fontsize=9.5)
    ax.grid(axis="x", color=comum.GRADE, linewidth=0.8)

    legenda = [Line2D([], [], marker="o", linestyle="", markersize=9,
                      color=comum.CORES[t], label=comum.ROTULO_TRATAMENTO[t])
               for t in comum.ORDEM_TRATAMENTO]
    ax.legend(handles=legenda, loc="upper right", frameon=False, fontsize=9.5)

    comum.titulo(ax, "RQ1 · Tempo e iterações até o green",
                 "Um ponto por trial do time. Rótulo: interações com o assistente até 10/10 testes.")
    fig.text(0.01, -0.04, f"Nota: {NOTA}.", fontsize=9.5, style="italic",
             color=comum.TEXTO_SECUNDARIO, transform=fig.transFigure)

    return comum.salvar(fig, "rq1-tempo-iteracoes.png")


if __name__ == "__main__":
    print(grafico_rq1())
