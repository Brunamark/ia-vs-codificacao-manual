#!/usr/bin/env bash
#
# retry_trials_pedro.sh
#
# Reexecução PONTUAL dos trials do Pedro que abortaram por falha de
# instrumentação (import esquecido em kata3-t1-manual e kata2-t4-ia), não por
# desempenho no algoritmo. Isso é uma EXCEÇÃO ao protocolo normal — um trial já
# cronometrado normalmente não pode ser refeito (DESIGN.md, ameaça #7,
# contaminação entre tentativas), mas falha de compilação por import é um
# problema de ambiente, não do tratamento em si.
#
# O script NÃO apaga o dado original: arquiva a linha antiga do results.csv e
# o log JSON antigo em output/logs/retries-pedro/ antes de rodar o trial de
# novo, para manter rastreabilidade da exceção.
#
# IMPORTANTE: documente esse retry no relatório final (ex.: "kata3-t1 e
# kata2-t4 refeitos por falha de instrumentação, não por desempenho").
#
# Uso (a partir da RAIZ do repositório):
#   ./rq1-rq2-tempo-e-testes/scripts/retry_trials_pedro.sh          # os 2 trials problemáticos
#   ./rq1-rq2-tempo-e-testes/scripts/retry_trials_pedro.sh 1        # só o trial 1 (kata3)
#   ./rq1-rq2-tempo-e-testes/scripts/retry_trials_pedro.sh 4        # só o trial 4 (kata2)
#
# Requisitos: JDK 17+, Maven no PATH, bash (Git Bash no Windows).

set -euo pipefail

MEMBER="pedro"
REPO_ROOT="$(pwd)"
KATAS_DIR="$REPO_ROOT/desenho-experimento/katas"
RQ1_DIR="$REPO_ROOT/rq1-rq2-tempo-e-testes"
RQ3_TRIALS_DIR="$REPO_ROOT/rq3-metricas-estaticas/trials"
RESULTS_CSV="$RQ1_DIR/output/results.csv"
LOGS_DIR="$RQ1_DIR/output/logs"
RETRY_ARCHIVE_DIR="$LOGS_DIR/retries-pedro"
WORK_BASE="${TMPDIR:-/tmp}/lab02-trials-$MEMBER-retry"

EXT_IA="anthropic.claude-code"

# Só os trials que abortaram por erro de import (DESIGN.md §7, linha do Pedro).
# kata_id | pasta do kata | trial_num | tratamento
TRIALS_ELEGIVEIS=(
  "kata3|kata3-calorias|1|manual"
  "kata2|kata2-torneio-luta|4|ia"
)

pause() {
  read -rp "$1 [Enter para continuar] " _
}

check_requisitos() {
  command -v mvn   >/dev/null 2>&1 || { echo "ERRO: mvn não encontrado no PATH."; exit 1; }
  command -v java  >/dev/null 2>&1 || { echo "ERRO: java não encontrado no PATH."; exit 1; }
  command -v javac >/dev/null 2>&1 || { echo "ERRO: javac não encontrado no PATH."; exit 1; }
  [ -d "$KATAS_DIR" ] || { echo "ERRO: $KATAS_DIR não existe. Rode este script na raiz do repositório."; exit 1; }
  [ -f "$RESULTS_CSV" ] || { echo "ERRO: $RESULTS_CSV não existe."; exit 1; }

  if [ ! -f "$RQ1_DIR/scripts/TrialTimer.class" ] \
     || [ "$RQ1_DIR/scripts/TrialTimer.java" -nt "$RQ1_DIR/scripts/TrialTimer.class" ]; then
    echo "Compilando o TrialTimer..."
    (cd "$RQ1_DIR" && javac scripts/TrialTimer.java)
  fi
}

preparar_ide() {
  local workdir="$1" tratamento="$2"

  mkdir -p "$workdir/.vscode"
  cat > "$workdir/.vscode/settings.json" <<'JSON'
{
  "editor.inlineSuggest.enabled": false,
  "github.copilot.enable": { "*": false },
  "chat.commandCenter.enabled": false,
  "java.completion.enabled": true
}
JSON

  if command -v code >/dev/null 2>&1; then
    echo "Abrindo o VS Code so nesta pasta, com $EXT_IA desabilitado nesta janela..."
    code --new-window --disable-extension "$EXT_IA" "$workdir" >/dev/null 2>&1 || true
  else
    echo "AVISO: 'code' nao esta no PATH. Abra manualmente:"
    echo "  code --new-window --disable-extension $EXT_IA \"$workdir\""
  fi

  echo
  echo "CONFIRA ANTES DE INICIAR O CRONOMETRO:"
  echo "  1. A janela aberta mostra SO a pasta do trial (nao o repositorio)."
  echo "  2. Nao rode testes pelo Test Explorer do Java durante o trial."
  if [ "$tratamento" = "manual" ]; then
    echo "  3. Tratamento MANUAL: nenhuma aba do claude.ai aberta no navegador."
  fi
}

# Arquiva (nao apaga) a linha antiga do results.csv e o log JSON antigo, para
# manter rastreabilidade de que esse trial foi refeito por excecao.
arquivar_dado_antigo() {
  local kata_id="$1" trial_num="$2" tratamento="$3"
  local timestamp_arquivo
  timestamp_arquivo="$(date +%Y%m%d_%H%M%S)"

  mkdir -p "$RETRY_ARCHIVE_DIR"

  local linha_antiga
  linha_antiga="$(grep ",${MEMBER},${kata_id},${trial_num}," "$RESULTS_CSV" || true)"

  if [ -z "$linha_antiga" ]; then
    echo "AVISO: nenhuma linha antiga encontrada em results.csv para ${MEMBER}/${kata_id}/trial ${trial_num} — seguindo mesmo assim."
    return 0
  fi

  {
    echo "# Retry de ${MEMBER}/${kata_id}/trial ${trial_num} (${tratamento}) em ${timestamp_arquivo}"
    echo "# Motivo: erro de compilacao por import esquecido (falha de instrumentacao, nao de desempenho)"
    echo "$linha_antiga"
  } >> "$RETRY_ARCHIVE_DIR/results_csv_linhas_substituidas.txt"
  echo "Linha antiga de results.csv arquivada em: $RETRY_ARCHIVE_DIR/results_csv_linhas_substituidas.txt"

  # Remove a linha antiga do results.csv (ela ja foi arquivada acima)
  local tmp_csv
  tmp_csv="$(mktemp)"
  grep -v ",${MEMBER},${kata_id},${trial_num}," "$RESULTS_CSV" > "$tmp_csv" || true
  mv "$tmp_csv" "$RESULTS_CSV"

  # Move (nao apaga) os logs JSON antigos desse trial
  local log_antigo
  for log_antigo in "$LOGS_DIR"/${kata_id}_t${trial_num}_${tratamento}_*.json; do
    [ -e "$log_antigo" ] || continue
    mv "$log_antigo" "$RETRY_ARCHIVE_DIR/"
    echo "Log antigo arquivado em: $RETRY_ARCHIVE_DIR/$(basename "$log_antigo")"
  done
}

executar_retry() {
  local kata_id="$1" kata_pasta="$2" trial_num="$3" tratamento="$4"
  local origem="$KATAS_DIR/$kata_pasta"
  local workdir="$WORK_BASE/trial${trial_num}-${kata_pasta}-${tratamento}"

  echo
  echo "================================================================"
  echo " RETRY — Trial $trial_num — $kata_pasta — tratamento: $tratamento"
  echo "================================================================"
  echo "Esse trial abortou anteriormente por erro de import (falha de"
  echo "instrumentação), não por desempenho no algoritmo. Vamos refazê-lo do zero."
  pause "Confirma que quer refazer este trial (isso ARQUIVA o dado antigo e roda de novo)?"

  arquivar_dado_antigo "$kata_id" "$trial_num" "$tratamento"

  if [ "$tratamento" = "manual" ]; then
    echo "!! TRATAMENTO MANUAL !!"
    echo "Desabilite o assistente de IA na IDE AGORA, ANTES de abrir a pasta."
    pause "Confirma que a IA já está desabilitada?"
  else
    echo "Tratamento IA: Claude via claude.ai (chat no navegador), modelo Sonnet 5."
    echo "  - Abra uma conversa NOVA e VAZIA, sem histórico deste projeto."
    echo "  - Aperte 'p' + Enter no TrialTimer a cada mensagem que você ENVIAR ao chat."
  fi

  rm -rf "$workdir"
  mkdir -p "$workdir"
  cp -r "$origem/." "$workdir/"
  echo "Kata copiado para (fora do repositório): $workdir"

  echo "Aquecendo o Maven (fora do cronômetro)..."
  (cd "$workdir" && mvn -q -B test-compile)

  echo
  preparar_ide "$workdir" "$tratamento"
  echo
  echo "Leia agora o enunciado:     $workdir/ENUNCIADO.md"
  echo "Lembrete: cheque os imports (java.util.*) antes de rodar os testes."
  pause "Quando terminar de ler e estiver pronto para começar, confirme"

  echo "Iniciando o TrialTimer. Durante o trial: p=prompt | s=status | q=abortar (só emergência)"
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

  echo
  echo "Limpando artefatos antes de copiar o código final..."
  (cd "$workdir" && mvn -q -B clean)
  rm -rf "$workdir/.vscode"

  local destino="$RQ3_TRIALS_DIR/${MEMBER}-${kata_pasta}-${tratamento}"
  rm -rf "$destino"
  mkdir -p "$destino"
  cp -r "$workdir/." "$destino/"
  echo "Código final (novo) copiado para: $destino"
}

main() {
  check_requisitos
  mkdir -p "$WORK_BASE"

  local selecionados=("$@")
  if [ ${#selecionados[@]} -eq 0 ]; then
    selecionados=(1 4)
  fi

  echo "Retry de trials problemáticos — integrante: $MEMBER"
  echo "Pasta de trabalho (fora do repositório): $WORK_BASE"
  echo
  echo "Trials elegíveis para retry (abortaram por erro de import):"
  for t in "${TRIALS_ELEGIVEIS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    local marca="   "
    for s in "${selecionados[@]}"; do
      [ "$s" = "$trial_num" ] && marca="-> "
    done
    echo "  ${marca}Trial $trial_num: $kata_pasta ($tratamento)"
  done
  echo
  echo "Isso vai ARQUIVAR (não apagar) o dado antigo em"
  echo "  $RETRY_ARCHIVE_DIR"
  echo "e rodar o trial de novo do zero, do enunciado até o código final."
  pause "Pronto para começar o retry?"

  for t in "${TRIALS_ELEGIVEIS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    for s in "${selecionados[@]}"; do
      if [ "$s" = "$trial_num" ]; then
        executar_retry "$kata_id" "$kata_pasta" "$trial_num" "$tratamento"
      fi
    done
  done

  echo
  echo "================================================================"
  echo " Retries executados: ${selecionados[*]}"
  echo " Próximos passos:"
  echo "  1. ./rq1-rq2-tempo-e-testes/scripts/check_trials_pedro.sh   (verificação)"
  echo "  2. Documentar no relatório final que esses trials foram refeitos por"
  echo "     falha de instrumentação (ver $RETRY_ARCHIVE_DIR)"
  echo "  3. git add + commit referenciando a Issue do Pedro (S02)"
  echo "================================================================"
}

main "$@"
