#!/usr/bin/env python3
"""
Dashboard das questões de pesquisa: gera um PNG por RQ em analise-final/output/graficos/.

Cada gráfico vive em seu próprio módulo, scripts/cards/cardN_*.py, que expõe uma
função grafico_rqN(). Este script descobre os cards presentes e publica essas
funções no próprio namespace, então `from dashboard import grafico_rq1` funciona
sem que os cards precisem editar este arquivo. Card ausente = gráfico não listado.

Uso (a partir da raiz do repositório):
    pip install -r analise-final/requirements.txt
    python analise-final/scripts/dashboard.py            # todos os gráficos disponíveis
    python analise-final/scripts/dashboard.py rq1 rq3    # apenas os escolhidos
    python analise-final/scripts/dashboard.py --listar
"""

import argparse
import importlib
import pkgutil
import sys
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent
if str(SCRIPTS) not in sys.path:
    sys.path.insert(0, str(SCRIPTS))

import cards  # noqa: E402


def descobrir_graficos() -> dict:
    """{'rq1': grafico_rq1, ...} para cada card presente em scripts/cards/."""
    graficos = {}
    for modulo in sorted(pkgutil.iter_modules(cards.__path__), key=lambda m: m.name):
        if not modulo.name.startswith("card"):
            continue
        mod = importlib.import_module(f"cards.{modulo.name}")
        for nome, obj in vars(mod).items():
            if nome.startswith("grafico_rq") and callable(obj):
                graficos[nome.removeprefix("grafico_")] = obj
    return graficos


GRAFICOS = descobrir_graficos()
globals().update({f"grafico_{rq}": funcao for rq, funcao in GRAFICOS.items()})


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("rqs", nargs="*", help="RQs a gerar (ex.: rq1 rq2); padrão: todas")
    parser.add_argument("--listar", action="store_true", help="lista os gráficos disponíveis e sai")
    args = parser.parse_args()

    if args.listar:
        print(", ".join(GRAFICOS) or "(nenhum card encontrado)")
        return

    desconhecidas = [rq for rq in args.rqs if rq not in GRAFICOS]
    if desconhecidas:
        sys.exit(f"Gráfico(s) indisponível(is): {', '.join(desconhecidas)}. "
                 f"Disponíveis: {', '.join(GRAFICOS) or 'nenhum'}")

    for rq in args.rqs or GRAFICOS:
        destino = GRAFICOS[rq]()
        print(f"[dashboard] {rq}: {destino}", file=sys.stderr)


if __name__ == "__main__":
    main()
