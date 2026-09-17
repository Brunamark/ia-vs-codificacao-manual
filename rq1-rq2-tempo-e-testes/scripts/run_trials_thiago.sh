#!/usr/bin/env bash
#
# run_trials_thiago.sh
#
# Roteiro guiado dos 4 trials do Thiago (Lab02 · S02 · Issue #8), na ordem e nos
# tratamentos definidos em desenho-experimento/DESIGN.md (seção 7):
#   Trial 1: kata2-torneio-luta  (ia)
#   Trial 2: kata1-cinema        (manual)
#   Trial 3: kata4-gastos        (manual)
#   Trial 4: kata3-calorias      (ia)
#
# O script NÃO resolve nada por você: ele copia o kata para fora do repositório,
# aquece o Maven fora do cronômetro, monta e roda o comando do TrialTimer e, ao
# final, limpa o build e copia o código para trials/ da RQ3. Ler o enunciado,
# desabilitar a IA no tratamento manual e codificar é com você.
#
# Uso (a partir da RAIZ do repositório):
#   ./rq1-rq2-tempo-e-testes/scripts/run_trials_thiago.sh        # os 4 trials
#   ./rq1-rq2-tempo-e-testes/scripts/run_trials_thiago.sh 1 2    # só os trials 1 e 2
#
# O DESIGN.md §9.2 forma os pares P5 (trials 1×2) e P6 (trials 4×3). Rode
# 1 e 2 na mesma sessão, e 3 e 4 na mesma sessão, para que fadiga e ambiente
# sejam comparáveis dentro de cada par.
#
# Requisitos: JDK 17+, Maven no PATH, bash (Git Bash no Windows — não roda em
# PowerShell/CMD).

set -euo pipefail

MEMBER="thiago"
REPO_ROOT="$(pwd)"
KATAS_DIR="$REPO_ROOT/desenho-experimento/katas"
RQ1_DIR="$REPO_ROOT/rq1-rq2-tempo-e-testes"
RQ3_TRIALS_DIR="$REPO_ROOT/rq3-metricas-estaticas/trials"
RESULTS_CSV="$RQ1_DIR/output/results.csv"
WORK_BASE="${TMPDIR:-/tmp}/lab02-trials-$MEMBER"

# Extensao de IA instalada no VS Code deste ambiente. Ela fica desabilitada nos
# QUATRO trials: no "manual" pelo motivo obvio, e no "ia" porque o tratamento
# decidido pelo trio e o chat do claude.ai, nao o assistente dentro da IDE
# (DESIGN.md §11). Duas ferramentas ativas invalidariam a instrumentacao.
EXT_IA="anthropic.claude-code"

# kata_id | pasta do kata | trial_num | tratamento   (DESIGN.md §7, linha do Thiago)
TRIALS=(
  "kata2|kata2-torneio-luta|1|ia"
  "kata1|kata1-cinema|2|manual"
  "kata4|kata4-gastos|3|manual"
  "kata3|kata3-calorias|4|ia"
)

pause() {
  read -rp "$1 [Enter para continuar] " _
}

check_requisitos() {
  command -v mvn  >/dev/null 2>&1 || { echo "ERRO: mvn não encontrado no PATH."; exit 1; }
  command -v java >/dev/null 2>&1 || { echo "ERRO: java não encontrado no PATH."; exit 1; }
  command -v javac >/dev/null 2>&1 || { echo "ERRO: javac não encontrado no PATH (precisa do JDK, não só do JRE)."; exit 1; }
  [ -d "$KATAS_DIR" ] || { echo "ERRO: $KATAS_DIR não existe. Rode este script na raiz do repositório."; exit 1; }

  # O .class é ignorado pelo git, então num clone limpo ele não existe e o
  # TrialTimer falharia com "Could not find or load main class TrialTimer".
  if [ ! -f "$RQ1_DIR/scripts/TrialTimer.class" ] \
     || [ "$RQ1_DIR/scripts/TrialTimer.java" -nt "$RQ1_DIR/scripts/TrialTimer.class" ]; then
    echo "Compilando o TrialTimer..."
    (cd "$RQ1_DIR" && javac scripts/TrialTimer.java)
  fi
}

# Blindagem da IDE: settings de workspace + abertura do VS Code com a extensao
# de IA desabilitada naquela janela. O .vscode/ e removido antes de copiar o
# codigo para trials/, para nao virar artefato da RQ3.
preparar_ide() {
  local workdir="$1" tratamento="$2"

  mkdir -p "$workdir/.vscode"
  cat > "$workdir/.vscode/settings.json" <<'JSON'
{
  // Trial do experimento Lab02 — NAO editar durante o trial.
  // Desliga qualquer sugestao inline generativa nesta pasta.
  "editor.inlineSuggest.enabled": false,
  "github.copilot.enable": { "*": false },
  "chat.commandCenter.enabled": false,
  // O IntelliSense do Java (redhat.java) continua LIGADO de proposito: e
  // completacao por simbolo, nao generativa — equivale a consultar a Javadoc,
  // que o DESIGN.md §5 permite no tratamento manual. Desliga-lo tornaria o
  // tratamento artificialmente mais dificil que "codificar sem IA".
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
  echo "  2. Abra src/main/java/kata/*.java, digite um comentario e espere 2 s:"
  echo "     NAO pode aparecer texto fantasma sugerindo codigo."
  echo "  3. Nao rode testes pelo Test Explorer do Java durante o trial —"
  echo "     o TrialTimer ja roda 'mvn -B test' a cada 5 s e dois builds"
  echo "     simultaneos disputam o target/."
  if [ "$tratamento" = "manual" ]; then
    echo "  4. Tratamento MANUAL: nenhuma aba do claude.ai aberta no navegador."
  fi
}

# Proteção contra re-execução acidental: um trial já cronometrado NÃO pode ser
# refeito (DESIGN.md, ameaça #7 — contaminação entre tratamentos).
ja_executado() {
  local kata_id="$1" trial_num="$2"
  [ -f "$RESULTS_CSV" ] || return 1
  grep -q ",${MEMBER},${kata_id},${trial_num}," "$RESULTS_CSV"
}

executar_trial() {
  local kata_id="$1" kata_pasta="$2" trial_num="$3" tratamento="$4"
  local origem="$KATAS_DIR/$kata_pasta"
  local workdir="$WORK_BASE/trial${trial_num}-${kata_pasta}-${tratamento}"

  echo
  echo "================================================================"
  echo " Trial $trial_num — $kata_pasta — tratamento: $tratamento"
  echo "================================================================"

  if ja_executado "$kata_id" "$trial_num"; then
    echo "!! ATENÇÃO: já existe uma linha para $MEMBER/$kata_id/trial $trial_num em"
    echo "   output/results.csv. Um trial já cronometrado NÃO pode ser refeito —"
    echo "   você já conhece a solução, o que viola a ameaça #7 do DESIGN.md."
    echo "   Pulando este trial."
    return 0
  fi

  if [ "$tratamento" = "manual" ]; then
    echo "!! TRATAMENTO MANUAL !!"
    echo "Desabilite o assistente de IA na IDE AGORA, ANTES de abrir a pasta."
    echo "Não basta não usar: o autocompletar inline age sem ser chamado."
    pause "Confirma que a IA já está desabilitada?"
  else
    echo "Tratamento IA: Claude via claude.ai (chat no navegador), modelo Sonnet 5."
    echo "  - Abra uma conversa NOVA e VAZIA, sem histórico deste projeto."
    echo "  - Cole o enunciado e o código manualmente: o chat não lê seus arquivos."
    echo "  - NUNCA cole nada vindo de desenho-experimento/solucoes-referencia/."
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
  echo
  echo "Dica: durante o trial, o TrialTimer roda 'mvn -B test' a cada 5 s e reescreve"
  echo "      $workdir/target/surefire-reports/*.txt"
  echo "      Deixe esse arquivo aberto no editor — é onde você vê QUAL teste falhou,"
  echo "      sem precisar rodar nenhum build em paralelo."
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
  rm -rf "$workdir/.vscode"   # config do trial, nao codigo produzido

  local destino="$RQ3_TRIALS_DIR/${MEMBER}-${kata_pasta}-${tratamento}"
  rm -rf "$destino"
  mkdir -p "$destino"
  cp -r "$workdir/." "$destino/"
  echo "Código final copiado para: $destino"
}

main() {
  check_requisitos
  mkdir -p "$WORK_BASE"

  # Sem argumentos = os 4 trials. Com argumentos = só os números informados.
  local selecionados=("$@")
  if [ ${#selecionados[@]} -eq 0 ]; then
    selecionados=(1 2 3 4)
  fi

  echo "Roteiro de trials — integrante: $MEMBER (Issue #8)"
  echo "Pasta de trabalho (fora do repositório): $WORK_BASE"
  echo
  echo "Sequência do Thiago (DESIGN.md, seção 7):"
  for t in "${TRIALS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    local marca="   "
    for s in "${selecionados[@]}"; do
      [ "$s" = "$trial_num" ] && marca="-> "
    done
    echo "  ${marca}Trial $trial_num: $kata_pasta ($tratamento)"
  done
  echo
  echo "Lembretes:"
  echo "  - Censura (estourar os 35 min) é DADO VÁLIDO. Nunca aperte 'q' para evitar registrar."
  echo "  - Não abra o repositório na IDE durante um trial; só a pasta de trabalho."
  echo "  - Não reabra desenho-experimento/solucoes-referencia/ antes nem durante os trials."
  echo "  - Use o VS Code (o script abre com a extensao de IA desabilitada)."
  echo "  - NAO use o IntelliJ: os plugins fullLine e ml-llm fazem completacao"
  echo "    generativa inline e vem LIGADOS por padrao."
  pause "Pronto para começar?"

  for t in "${TRIALS[@]}"; do
    IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
    for s in "${selecionados[@]}"; do
      if [ "$s" = "$trial_num" ]; then
        executar_trial "$kata_id" "$kata_pasta" "$trial_num" "$tratamento"
      fi
    done
  done

  echo
  echo "================================================================"
  echo " Trials executados: ${selecionados[*]}"
  echo " Próximos passos:"
  echo "  1. ./rq1-rq2-tempo-e-testes/scripts/check_trials_thiago.sh   (verificação)"
  echo "  2. git add + commit referenciando a Issue #8"
  echo "================================================================"
}

main "$@"
