#!/usr/bin/env python3
"""
Coleta métricas estáticas (WMC médio por método, % de linhas duplicadas e LOC)
para cada trial (kata) e consolida o resultado em um único CSV.

Uso (rodar a partir de rq3-metricas-estaticas/):
    python scripts/collect_metrics.py \
        --trials-dir trials \
        --ck-jar tools/ck-src/target/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar \
        --pmd-bin tools/pmd-bin-7.27.0/bin/pmd \
        --output output/metrics.csv

Cada subdiretório direto de --trials-dir é tratado como um trial. O nome do
trial no CSV de saída é o nome desse subdiretório.
"""

import argparse
import csv
import os
import statistics
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

JAVA_SUFFIX = ".java"
# No Windows, scripts .bat (como pmd.bat) só são resolvidos pelo CreateProcess
# via shell; em outros sistemas o shell é desnecessário.
USA_SHELL = os.name == "nt"


def executar(comando: list[str]) -> subprocess.CompletedProcess:
    return subprocess.run(comando, capture_output=True, text=True, shell=USA_SHELL)


def strip_ns(tag: str) -> str:
    """Remove o namespace XML de uma tag ('{ns}duplication' -> 'duplication')."""
    return tag.split("}", 1)[-1]


def listar_trials(trials_dir: Path) -> list[Path]:
    return sorted(p for p in trials_dir.iterdir() if p.is_dir())


def contar_loc(trial_dir: Path) -> int:
    """LOC bruto: total de linhas físicas (incluindo comentários e linhas em
    branco) somado em todos os arquivos .java do trial."""
    total = 0
    for arquivo in trial_dir.rglob(f"*{JAVA_SUFFIX}"):
        with arquivo.open("r", encoding="utf-8", errors="replace") as f:
            total += sum(1 for _ in f)
    return total


def rodar_ck(java_bin: str, ck_jar: Path, trial_dir: Path, saida_dir: Path) -> None:
    saida_dir.mkdir(parents=True, exist_ok=True)
    comando = [
        java_bin, "-jar", str(ck_jar),
        str(trial_dir),
        "false",   # useJars
        "0",       # maxAtOnce (0 = automático)
        "false",   # variablesAndFieldsMetrics
        str(saida_dir) + "/",
    ]
    resultado = executar(comando)
    log = saida_dir / "ck.log"
    log.write_text(resultado.stdout + "\n" + resultado.stderr, encoding="utf-8")
    if resultado.returncode != 0:
        raise RuntimeError(
            f"CK falhou (exit {resultado.returncode}) para '{trial_dir.name}'. "
            f"Veja {log}"
        )


def wmc_medio_por_metodo(ck_saida_dir: Path) -> tuple[float, int]:
    method_csv = ck_saida_dir / "method.csv"
    if not method_csv.exists():
        raise FileNotFoundError(f"method.csv não encontrado em {ck_saida_dir}")
    valores = []
    with method_csv.open(encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            valores.append(float(linha["wmc"]))
    if not valores:
        return 0.0, 0
    return statistics.mean(valores), len(valores)


def rodar_cpd(pmd_bin: str, trial_dir: Path, min_tokens: int, saida_xml: Path) -> None:
    saida_xml.parent.mkdir(parents=True, exist_ok=True)
    # cmd.exe (usado via shell=True no Windows) não resolve caminhos relativos
    # com barra normal ("/") como executável; caminho absoluto resolve isso
    # independente de o caminho ter sido passado com "/" ou "\".
    pmd_bin_resolvido = str(Path(pmd_bin).resolve()) if USA_SHELL else pmd_bin
    comando = [
        pmd_bin_resolvido, "cpd",
        "--minimum-tokens", str(min_tokens),
        "--dir", str(trial_dir),
        "--language", "java",
        "--format", "xml",
        "--no-fail-on-violation",
    ]
    resultado = executar(comando)
    if resultado.returncode != 0:
        log = saida_xml.with_suffix(".log")
        log.write_text(resultado.stdout + "\n" + resultado.stderr, encoding="utf-8")
        raise RuntimeError(
            f"PMD CPD falhou (exit {resultado.returncode}) para '{trial_dir.name}'. "
            f"Veja {log}"
        )
    saida_xml.write_text(resultado.stdout, encoding="utf-8")


def percentual_duplicacao(cpd_xml: Path, loc_total: int) -> tuple[float, int]:
    """% de linhas duplicadas = soma de (linhas * ocorrências) de cada bloco
    duplicado reportado pelo CPD, dividido pelo LOC total do trial.

    Observação de metodologia: cada ocorrência de um bloco duplicado (incluindo
    a primeira) é contada, então blocos duplicados sobrepostos podem fazer o
    percentual ultrapassar 100%. Para os fins da RQ3 (comparação relativa entre
    trials), essa aproximação é suficiente e é a mesma usada em todos os trials.
    """
    if loc_total == 0:
        return 0.0, 0
    conteudo = cpd_xml.read_text(encoding="utf-8").strip()
    if not conteudo:
        return 0.0, 0
    raiz = ET.fromstring(conteudo)
    linhas_duplicadas = 0
    for elemento in raiz:
        if strip_ns(elemento.tag) != "duplication":
            continue
        linhas = int(elemento.attrib["lines"])
        ocorrencias = sum(
            1 for filho in elemento if strip_ns(filho.tag) == "file"
        )
        linhas_duplicadas += linhas * ocorrencias
    percentual = (linhas_duplicadas / loc_total) * 100
    return percentual, linhas_duplicadas


def processar_trial(
    trial_dir: Path,
    java_bin: str,
    ck_jar: Path,
    pmd_bin: str,
    min_tokens: int,
    work_dir: Path,
) -> dict:
    print(f"[collect-metrics] processando trial '{trial_dir.name}'...", file=sys.stderr)

    ck_saida_dir = work_dir / trial_dir.name / "ck"
    rodar_ck(java_bin, ck_jar, trial_dir, ck_saida_dir)
    wmc_medio, qtd_metodos = wmc_medio_por_metodo(ck_saida_dir)

    loc_total = contar_loc(trial_dir)

    cpd_xml = work_dir / trial_dir.name / "cpd" / "cpd.xml"
    rodar_cpd(pmd_bin, trial_dir, min_tokens, cpd_xml)
    duplicacao_pct, linhas_duplicadas = percentual_duplicacao(cpd_xml, loc_total)

    return {
        "trial": trial_dir.name,
        "loc": loc_total,
        "qtd_metodos": qtd_metodos,
        "wmc_medio_por_metodo": round(wmc_medio, 3),
        "linhas_duplicadas": linhas_duplicadas,
        "duplicacao_percentual": round(duplicacao_pct, 3),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--trials-dir", type=Path, default=Path("trials"))
    parser.add_argument("--ck-jar", type=Path, required=True)
    parser.add_argument("--pmd-bin", type=str, required=True, help="Caminho para o executável pmd/pmd.bat")
    parser.add_argument("--java-bin", type=str, default="java")
    parser.add_argument("--min-tokens", type=int, default=50, help="--minimum-tokens do PMD CPD (default: 50)")
    parser.add_argument("--output", type=Path, default=Path("output/metrics.csv"))
    parser.add_argument("--work-dir", type=Path, default=Path("output/raw"))
    args = parser.parse_args()

    trials = listar_trials(args.trials_dir)
    if not trials:
        sys.exit(f"Nenhum trial encontrado em '{args.trials_dir}'")

    linhas = [
        processar_trial(trial, args.java_bin, args.ck_jar, args.pmd_bin, args.min_tokens, args.work_dir)
        for trial in trials
    ]

    args.output.parent.mkdir(parents=True, exist_ok=True)
    campos = ["trial", "loc", "qtd_metodos", "wmc_medio_por_metodo", "linhas_duplicadas", "duplicacao_percentual"]
    with args.output.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=campos)
        writer.writeheader()
        writer.writerows(linhas)

    print(f"[collect-metrics] CSV consolidado gerado em: {args.output}", file=sys.stderr)


if __name__ == "__main__":
    main()
