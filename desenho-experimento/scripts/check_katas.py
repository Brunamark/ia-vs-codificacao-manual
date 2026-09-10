#!/usr/bin/env python3
"""
Valida os katas do experimento como objeto experimental, antes da execução da S02.

Para cada kata, o script faz três verificações:

  1. GREEN NA REFERÊNCIA — copia o esqueleto para um diretório temporário, sobrepõe a solução
     de referência e roda os testes de aceitação. Todos precisam passar.
  2. ESQUELETO FALHA — roda os testes sobre o esqueleto puro (sem solução) e exige que falhem.
     Um kata cujos testes passam sem implementação tem testes vazios e invalidaria a RQ2.
  3. INTEGRIDADE DOS TESTES — confere o SHA-256 de cada arquivo de teste contra o
     solucoes-referencia/MANIFEST.sha256, garantindo que ninguém alterou os testes.

O parsing da saída do Maven usa o MESMO padrão que o TrialTimer da RQ1/RQ2
("Tests run: X, Failures: Y, Errors: Z"), de propósito: se este script consegue ler o
resultado, o TrialTimer também consegue. Isso torna a validação um teste de integração real
entre os dois artefatos, e não apenas "os testes passam".

Uso (rodar a partir de desenho-experimento/):
    python scripts/check_katas.py \
        --katas-dir katas \
        --solucoes-dir solucoes-referencia \
        --output output/validacao-katas.csv

Código de saída: 0 se todos os katas ficam OK; 1 se qualquer um falha.
"""

import argparse
import csv
import hashlib
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

JAVA_SUFFIX = ".java"
MANIFEST = "MANIFEST.sha256"
CAMINHO_FONTE = Path("src/main/java/kata")
CAMINHO_TESTES = Path("src/test/java/kata")

# No Windows, scripts .cmd (como mvn.cmd) só são resolvidos pelo CreateProcess via shell;
# em outros sistemas o shell é desnecessário. Mesmo tratamento do collect_metrics.py (RQ3).
USA_SHELL = os.name == "nt"

# Padrões do Maven Surefire — idênticos aos do TrialTimer (RQ1/RQ2).
TESTS_RUN = re.compile(r"Tests run:\s*(\d+)")
FAILURES = re.compile(r"Failures:\s*(\d+)")
ERRORS = re.compile(r"Errors:\s*(\d+)")


def executar(comando: list[str], cwd: Path) -> subprocess.CompletedProcess:
    return subprocess.run(
        comando, cwd=str(cwd), capture_output=True, text=True,
        encoding="utf-8", errors="replace", shell=USA_SHELL,
    )


def ultimo_int(padrao: re.Pattern, texto: str) -> int:
    """Última ocorrência do padrão — no Surefire é a linha agregada de 'Results'."""
    achados = padrao.findall(texto)
    return int(achados[-1]) if achados else 0


def listar_katas(katas_dir: Path) -> list[Path]:
    return sorted(p for p in katas_dir.iterdir() if p.is_dir())


def hash_normalizado(arquivo: Path) -> str:
    """SHA-256 do conteúdo com quebras de linha normalizadas para LF.

    O Git converte LF em CRLF ao fazer checkout no Windows, então o hash do arquivo bruto
    mudaria conforme o sistema operacional do integrante. Normalizar mantém o manifesto
    válido nas três máquinas do grupo.
    """
    conteudo = arquivo.read_bytes().replace(b"\r\n", b"\n")
    return hashlib.sha256(conteudo).hexdigest()


def ler_manifesto(caminho: Path) -> dict[str, str]:
    """Lê o MANIFEST.sha256 no formato '<hash>  <caminho relativo>'."""
    if not caminho.exists():
        return {}
    esperado = {}
    for linha in caminho.read_text(encoding="utf-8").splitlines():
        linha = linha.strip()
        if not linha or linha.startswith("#"):
            continue
        digest, _, relativo = linha.partition(" ")
        esperado[relativo.strip()] = digest
    return esperado


def verificar_integridade(kata_dir: Path, raiz: Path, esperado: dict[str, str]) -> bool:
    """Confere o hash de todos os arquivos de teste do kata contra o manifesto."""
    testes = sorted((kata_dir / CAMINHO_TESTES).glob(f"*{JAVA_SUFFIX}"))
    if not testes:
        return False
    for teste in testes:
        chave = teste.relative_to(raiz).as_posix()
        if esperado.get(chave) != hash_normalizado(teste):
            return False
    return True


def preparar_copia(kata_dir: Path, destino: Path, solucao_dir: Path | None) -> Path:
    """Copia o esqueleto e, se houver, sobrepõe a solução de referência."""
    projeto = destino / kata_dir.name
    shutil.copytree(kata_dir, projeto)
    if solucao_dir is not None:
        for fonte in solucao_dir.glob(f"*{JAVA_SUFFIX}"):
            shutil.copy2(fonte, projeto / CAMINHO_FONTE / fonte.name)
    return projeto


def rodar_testes(mvn_bin: str, projeto: Path, offline: bool) -> tuple[int, str]:
    mvn_resolvido = str(Path(mvn_bin).resolve()) if USA_SHELL and Path(mvn_bin).exists() else mvn_bin
    comando = [mvn_resolvido, "-B", "test"]
    if offline:
        comando.insert(1, "-o")
    resultado = executar(comando, projeto)
    return resultado.returncode, resultado.stdout + "\n" + resultado.stderr


def salvar_log(work_dir: Path, kata: str, fase: str, saida: str) -> Path:
    work_dir.mkdir(parents=True, exist_ok=True)
    log = work_dir / f"{kata}-{fase}.log"
    log.write_text(saida, encoding="utf-8")
    return log


def processar_kata(
    kata_dir: Path,
    raiz: Path,
    solucoes_dir: Path,
    mvn_bin: str,
    offline: bool,
    checar_esqueleto: bool,
    work_dir: Path,
    esperado: dict[str, str],
) -> dict:
    nome = kata_dir.name
    print(f"[check-katas] validando '{nome}'...", file=sys.stderr)
    inicio = time.monotonic()

    hash_ok = verificar_integridade(kata_dir, raiz, esperado)

    solucao_dir = solucoes_dir / nome
    if not solucao_dir.is_dir():
        raise FileNotFoundError(f"Solução de referência não encontrada: {solucao_dir}")

    with tempfile.TemporaryDirectory(prefix="check-katas-") as tmp:
        projeto = preparar_copia(kata_dir, Path(tmp), solucao_dir)
        codigo, saida = rodar_testes(mvn_bin, projeto, offline)

    total = ultimo_int(TESTS_RUN, saida)
    falhas = ultimo_int(FAILURES, saida)
    erros = ultimo_int(ERRORS, saida)
    passando = total - falhas - erros
    referencia_green = codigo == 0 and total > 0 and falhas == 0 and erros == 0
    if not referencia_green:
        salvar_log(work_dir, nome, "referencia", saida)

    if checar_esqueleto:
        with tempfile.TemporaryDirectory(prefix="check-katas-") as tmp:
            projeto = preparar_copia(kata_dir, Path(tmp), None)
            codigo_esq, saida_esq = rodar_testes(mvn_bin, projeto, offline)
        esqueleto_falha = codigo_esq != 0
        if not esqueleto_falha:
            salvar_log(work_dir, nome, "esqueleto", saida_esq)
        esqueleto_col = str(esqueleto_falha).lower()
    else:
        esqueleto_falha = True
        esqueleto_col = "pulado"

    ok = referencia_green and esqueleto_falha and hash_ok
    return {
        "kata": nome,
        "tests_total": total,
        "tests_passing": passando,
        "tests_failures": falhas + erros,
        "referencia_green": str(referencia_green).lower(),
        "esqueleto_falha": esqueleto_col,
        "hash_ok": str(hash_ok).lower(),
        "duracao_seg": round(time.monotonic() - inicio, 1),
        "status": "OK" if ok else "FALHOU",
    }


def main() -> None:
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--katas-dir", type=Path, default=Path("katas"))
    parser.add_argument("--solucoes-dir", type=Path, default=Path("solucoes-referencia"))
    parser.add_argument("--mvn-bin", type=str, default="mvn",
                        help="Caminho para o executável mvn/mvn.cmd (default: mvn)")
    parser.add_argument("--output", type=Path, default=Path("output/validacao-katas.csv"))
    parser.add_argument("--work-dir", type=Path, default=Path("output/raw"),
                        help="Onde ficam os logs do Maven quando uma verificação falha")
    parser.add_argument("--offline", action="store_true",
                        help="Passa -o ao Maven (exige ~/.m2 já aquecido)")
    parser.add_argument("--skip-skeleton-check", action="store_true",
                        help="Pula a checagem 'esqueleto deve falhar' (só para depuração)")
    args = parser.parse_args()

    katas = listar_katas(args.katas_dir)
    if not katas:
        sys.exit(f"Nenhum kata encontrado em '{args.katas_dir}'")

    raiz = args.katas_dir.parent
    esperado = ler_manifesto(args.solucoes_dir / MANIFEST)
    if not esperado:
        print(f"[check-katas] AVISO: {MANIFEST} ausente ou vazio — hash_ok sairá 'false'",
              file=sys.stderr)

    linhas = [
        processar_kata(kata, raiz, args.solucoes_dir, args.mvn_bin, args.offline,
                       not args.skip_skeleton_check, args.work_dir, esperado)
        for kata in katas
    ]

    args.output.parent.mkdir(parents=True, exist_ok=True)
    campos = ["kata", "tests_total", "tests_passing", "tests_failures", "referencia_green",
              "esqueleto_falha", "hash_ok", "duracao_seg", "status"]
    with args.output.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=campos)
        writer.writeheader()
        writer.writerows(linhas)

    falharam = [linha["kata"] for linha in linhas if linha["status"] != "OK"]
    print(f"[check-katas] CSV gerado em: {args.output}", file=sys.stderr)
    if falharam:
        sys.exit(f"[check-katas] katas com problema: {', '.join(falharam)}")
    print(f"[check-katas] {len(linhas)} katas validados com sucesso.", file=sys.stderr)


if __name__ == "__main__":
    main()
