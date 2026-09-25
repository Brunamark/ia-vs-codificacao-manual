"""
Base compartilhada pelos cards do dashboard: caminhos, paleta, rótulos dos katas
e utilitários de leitura/formatação/gravação.

Nenhum card depende de outro, apenas deste módulo. Para adicionar um gráfico,
crie cards/cardN_<rq>_<tema>.py com uma função grafico_rqN() -> Path; o
dashboard.py a descobre sozinho.

Fonte única dos dados: o relatório final do time (RELATORIO_REFINADO). Cada card
lê um CSV em analise-final/output/ transcrito de uma tabela do relatório, nunca
de outros artefatos do repositório.
"""

import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # gera PNG sem precisar de display

import matplotlib.pyplot as plt  # noqa: E402

ANALISE_FINAL = Path(__file__).resolve().parents[2]
OUTPUT = ANALISE_FINAL / "output"
GRAFICOS = OUTPUT / "graficos"

# Paleta categórica validada (slots 1 e 2), fixa por tratamento nos três gráficos.
CORES = {"ia": "#2a78d6", "manual": "#eb6834"}
CORES_CLARAS = {"ia": "#aac9ef", "manual": "#f7c2ad"}
ROTULO_TRATAMENTO = {"ia": "IA", "manual": "Manual"}
ORDEM_TRATAMENTO = ("ia", "manual")

SUPERFICIE = "#fcfcfb"
TEXTO = "#0b0b0b"
TEXTO_SECUNDARIO = "#52514e"
GRADE = "#e4e3df"

KATAS = {
    "kata1": "K1 · cinema",
    "kata2": "K2 · torneio",
    "kata3": "K3 · calorias",
    "kata4": "K4 · gastos",
}


def ler_csv(caminho: Path) -> list[dict]:
    with caminho.open(encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def parse_alvo(alvo: str) -> tuple[str, str]:
    """'kata2-torneio-luta-ia' -> ('kata2', 'ia')."""
    partes = alvo.split("-")
    return partes[0], partes[-1]


def chave_ordem(tratamento: str, kata: str) -> tuple[int, str]:
    """Ordena por tratamento (IA primeiro, como no relatório) e depois por kata."""
    return ORDEM_TRATAMENTO.index(tratamento), kata


def fmt_br(valor: float, casas: int = 1) -> str:
    """Decimal com vírgula, como no relatório (55.9 -> '55,9')."""
    return f"{valor:.{casas}f}".replace(".", ",")


def nova_figura(largura: float, altura: float):
    fig, ax = plt.subplots(figsize=(largura, altura))
    fig.patch.set_facecolor(SUPERFICIE)
    ax.set_facecolor(SUPERFICIE)
    for lado in ("top", "right"):
        ax.spines[lado].set_visible(False)
    for lado in ("left", "bottom"):
        ax.spines[lado].set_color(GRADE)
    ax.tick_params(colors=TEXTO_SECUNDARIO, labelsize=9)
    ax.set_axisbelow(True)
    return fig, ax


def titulo(ax, texto: str, subtitulo: str) -> None:
    ax.set_title(subtitulo, loc="left", fontsize=9.5, color=TEXTO_SECUNDARIO, pad=10)
    ax.figure.suptitle(texto, x=ax.get_position().x0, ha="left",
                       fontsize=13, fontweight="bold", color=TEXTO)


def salvar(fig, nome_png: str) -> Path:
    GRAFICOS.mkdir(parents=True, exist_ok=True)
    destino = GRAFICOS / nome_png
    fig.savefig(destino, dpi=150, bbox_inches="tight", facecolor=fig.get_facecolor())
    plt.close(fig)
    return destino
