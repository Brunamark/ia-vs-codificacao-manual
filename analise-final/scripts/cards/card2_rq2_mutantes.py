"""
Card 2 — RQ2: mutation score por trial.

As métricas originais de defeito saturaram (100% de aceitação, suíte oculta e
cobertura em todos os trials); o mutation score foi a única que discriminou.
Barras horizontais empilhadas com a contagem absoluta de mutantes mortos e
sobreviventes, porque o nº de mutantes varia de 11 a 25 entre os trials.

Dados: output/qualidade-dinamica.csv, transcrito da tabela da seção 6.2 do relatório.

Uso isolado: python analise-final/scripts/cards/card2_rq2_mutantes.py
"""

import sys
from pathlib import Path

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from matplotlib.patches import Patch  # noqa: E402
from matplotlib.ticker import MaxNLocator  # noqa: E402

from cards import comum  # noqa: E402

QUALIDADE_CSV = comum.OUTPUT / "qualidade-dinamica.csv"
HACHURA = "////"


def grafico_rq2() -> Path:
    trials = []
    for linha in comum.ler_csv(QUALIDADE_CSV):
        kata, tratamento = comum.parse_alvo(linha["alvo"])
        trials.append({
            "kata": kata,
            "tratamento": tratamento,
            "mutantes": int(linha["mutantes"]),
            "mortos": int(linha["mutantes_mortos"]),
            "sobreviventes": int(linha["mutantes_sobreviventes"]),
            "score": float(linha["mutation_score"]),
        })
    trials.sort(key=lambda t: comum.chave_ordem(t["tratamento"], t["kata"]))

    fig, ax = comum.nova_figura(9, 4.2)
    posicoes = range(len(trials))[::-1]  # primeiro trial no topo
    altura = 0.56

    for y, t in zip(posicoes, trials):
        cor = comum.CORES[t["tratamento"]]
        ax.barh(y, t["mortos"], height=altura, color=cor,
                edgecolor=comum.SUPERFICIE, linewidth=2, zorder=3)
        if t["sobreviventes"]:
            ax.barh(y, t["sobreviventes"], left=t["mortos"], height=altura,
                    color=comum.CORES_CLARAS[t["tratamento"]], hatch=HACHURA,
                    edgecolor=cor, linewidth=0, zorder=3)
            ax.barh(y, t["sobreviventes"], left=t["mortos"], height=altura,
                    fill=False, edgecolor=comum.SUPERFICIE, linewidth=2, zorder=4)

        ax.text(t["mortos"] / 2, y, str(t["mortos"]), ha="center", va="center",
                fontsize=9.5, fontweight="bold", color="white", zorder=5)
        if t["sobreviventes"]:
            ax.text(t["mortos"] + t["sobreviventes"] / 2, y, str(t["sobreviventes"]),
                    ha="center", va="center", fontsize=9.5, fontweight="bold",
                    color=comum.TEXTO, zorder=5,
                    bbox=dict(boxstyle="round,pad=0.15", facecolor=comum.SUPERFICIE,
                              edgecolor="none"))

        ax.annotate(f"{comum.fmt_br(t['score'] * 100)}%  ({t['mortos']}/{t['mutantes']})",
                    (t["mutantes"], y), xytext=(8, 0), textcoords="offset points",
                    va="center", fontsize=10, fontweight="bold", color=comum.TEXTO)

    ax.set_yticks(list(posicoes))
    ax.set_yticklabels([f"{comum.KATAS[t['kata']]} · {comum.ROTULO_TRATAMENTO[t['tratamento']]}"
                        for t in trials])
    ax.set_xlim(0, max(t["mutantes"] for t in trials) * 1.3)
    ax.xaxis.set_major_locator(MaxNLocator(integer=True))
    ax.set_xlabel("mutantes gerados pelo PIT (contagem absoluta)",
                  color=comum.TEXTO_SECUNDARIO, fontsize=9.5)
    ax.grid(axis="x", color=comum.GRADE, linewidth=0.8)

    legenda = [
        *(Patch(facecolor=comum.CORES[t], label=comum.ROTULO_TRATAMENTO[t])
          for t in comum.ORDEM_TRATAMENTO),
        Patch(facecolor=comum.TEXTO_SECUNDARIO, label="mortos (sólido)"),
        Patch(facecolor="#dddcd8", edgecolor=comum.TEXTO_SECUNDARIO, hatch=HACHURA,
              linewidth=0, label="sobreviventes (hachurado)"),
    ]
    ax.legend(handles=legenda, loc="center left", bbox_to_anchor=(1.01, 0.5),
              frameon=False, fontsize=9)

    comum.titulo(ax, "RQ2 · Mutation score por trial",
                 "Única métrica de defeito que discriminou: aceitação, suíte oculta e cobertura "
                 "ficaram em 100%.")
    fig.text(0.01, -0.04,
             "Nota: mutante sobrevivente não é defeito; é lógica cujo comportamento nenhum "
             "teste fixa.", fontsize=9.5, style="italic", color=comum.TEXTO_SECUNDARIO,
             transform=fig.transFigure)

    return comum.salvar(fig, "rq2-mutantes.png")


if __name__ == "__main__":
    print(grafico_rq2())
