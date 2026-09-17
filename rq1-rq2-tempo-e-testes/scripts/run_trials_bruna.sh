#!/usr/bin/env bash
#
# run_trials_bruna.sh
#
# Roteiro guiado dos 4 trials da Bruna (Lab02 · S02), na ordem e nos
# tratamentos definidos em desenho-experimento/DESIGN.md (seção 7):
#   Trial 1: kata1-cinema        (ia)
#   Trial 2: kata2-torneio-luta  (manual)
#   Trial 3: kata3-calorias      (ia)
#   Trial 4: kata4-gastos        (manual)
#
# O script NÃO decide nada por você: ele copia o kata, aquece o Maven,
# monta e roda o comando do TrialTimer, e no final copia o código para
# o lugar certo da RQ3. Todo o resto (ler o enunciado, desabilitar a IA
# no tratamento manual, codificar) é com você.
#
# Uso:
#   1. Rode a partir da RAIZ do repositório clonado.
#   2. chmod +x scripts/run_trials_bruna.sh
#   3. ./scripts/run_trials_bruna.sh
#
# Requisitos: JDK 17+, Maven no PATH (mvn), bash.

set -euo pipefail

MEMBER="bruna"
REPO_ROOT="$(pwd)"
KATAS_DIR="$REPO_ROOT/desenho-experimento/katas"
RQ1_DIR="$REPO_ROOT/rq1-rq2-tempo-e-testes"
RQ3_TRIALS_DIR="$REPO_ROOT/rq3-metricas-estaticas/trials"
WORK_BASE="${TMPDIR:-/tmp}/lab02-trials-$MEMBER"

# kata_id | pasta do kata | trial_num | tratamento
TRIALS=(
  "kata1|kata1-cinema|1|ia"
  "kata2|kata2-torneio-luta|2|manual"
  "kata3|kata3-calorias|3|ia"
  "kata4|kata4-gastos|4|manual"
)

pause() {
  read -rp "$1 [Enter para continuar] " _
}

check_requisitos() {
  command -v mvn >/dev/null 2>&1 || { echo "ERRO: mvn não encontrado no PATH."; exit 1; }
  command -v java >/dev/null 2>&1 || { echo "ERRO: java não encontrado no PATH."; exit 1; }
  [ -d "$KATAS_DIR" ] || { echo "ERRO: $KATAS_DIR não existe. Rode este script na raiz do repositório."; exit 1; }
}

executar_trial() {
  local kata_id="$1" kata_pasta="$2" trial_num="$3" tratamento="$4"
  local origem="$KATAS_DIR/$kata_pasta"
  local workdir="$WORK_BASE/trial${trial_num}-${kata_pasta}-${tratamento}"

  echo
  echo "================================================================"
  echo " Trial $trial_num — $kata_pasta — tratamento: $tratamento"
  echo "================================================================"

  if [ "$tratamento" = "manual" ]; then
    echo "!! TRATAMENTO MANUAL !!"
    echo "Desabilite o assistente de IA na IDE AGORA, antes de continuar."
    pause "Confirma que a IA já está desabilitada?"
  else
    echo "Tratamento IA: assistente liberado. Lembre de apertar 'p' + Enter"
    echo "no TrialTimer a cada interação com o Claude."
  fi

  rm -rf "$workdir"
  mkdir -p "$workdir"
  cp -r "$origem/." "$workdir/"
  echo "Kata copiado para: $workdir"

  echo "Aquecendo o Maven (fora do cronômetro)..."
  (cd "$workdir" && mvn -q -B test-compile)

  echo
  echo "Leia agora o ENUNCIADO.md em: $workdir/ENUNCIADO.md"
  pause "Quando terminar de ler e estiver pronta para começar, confirme"

  echo "Iniciando o TrialTimer. Comandos durante o trial: p=prompt, s=status, q=abortar"
  echo

  (
    cd "$RQ1_DIR"
    java -cp scripts TrialTimer \
      --member "$MEMBER" \
      --kata "$kata_id" \
      --trial "$trial_num" \
      --treatment "$tratamento" \
      --test-cmd "mvn -B test" \
      --cwd "$workdir"
  )

  local destino="$RQ3_TRIALS_DIR/${MEMBER}-${kata_pasta}-${tratamento}"
  rm -rf "$destino"
  mkdir -p "$destino"
  cp -r "$workdir/." "$destino/"
  echo "Código final copiado para: $destino"
  echo "(revise o conteúdo antes de commitar — remova o target/ se tiver sido gerado)"
}

main() {
  check_requisitos
  mkdir -p "$WORK_BASE"

  echo "Roteiro de trials — integrante: $MEMBER"
  echo "Pasta de trabalho (fora do repositório): $WORK_BASE"
  echo
  echo "Ordem dos 4 trials (DESIGN.md, seção 7):"
  for t in "${TRIALS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    echo "  Trial $trial_num: $kata_pasta ($tratamento)"
  done
  pause "Pronta para começar?"

  for t in "${TRIALS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    executar_trial "$kata_id" "$kata_pasta" "$trial_num" "$tratamento"
  done

  echo
  echo "================================================================"
  echo " Os 4 trials da Bruna foram executados."
  echo " Próximos passos manuais:"
  echo "  1. Conferir rq1-rq2-tempo-e-testes/output/results.csv (4 novas linhas)"
  echo "  2. Conferir as 4 pastas em rq3-metricas-estaticas/trials/bruna-*"
  echo "  3. git add + commit referenciando a Issue da S02"
  echo "================================================================"
}

main "$@"
