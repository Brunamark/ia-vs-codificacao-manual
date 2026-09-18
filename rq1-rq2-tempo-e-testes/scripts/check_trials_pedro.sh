#!/usr/bin/env bash
#
# check_trials_pedro.sh
#
# Verificação dos trials do Pedro (Lab02 · S02) contra o protocolo
# do experimento (desenho-experimento/DESIGN.md, seções 5, 7 e 12).
#
# Rodar depois de CADA trial — pega problema de ambiente cedo, enquanto ainda dá
# para corrigir — e de novo antes de commitar.
#
# Confere, por trial: tratamento e kata segundo a sequência da §7; test_cmd sem
# -q; time-box de 35 min; leitura do Surefire (tests_total); censura e abort;
# contagem de prompts coerente com o tratamento; log JSON presente; código final
# copiado para trials/ sem target/ versionável; e integridade dos arquivos de
# teste, que são imutáveis (§5).
#
# Confere, no geral: se results.csv e output/logs/ estão versionáveis pelo git
# (são dado bruto, §13) e se a instrumentação do assistente foi registrada (§11).
#
# Uso (a partir da RAIZ do repositório):
#   ./rq1-rq2-tempo-e-testes/scripts/check_trials_pedro.sh
#
# Saída: relatório por trial + código de saída 0 (tudo OK) ou 1 (algo a corrigir).

set -uo pipefail

MEMBER="pedro"
REPO_ROOT="$(pwd)"
KATAS_DIR="$REPO_ROOT/desenho-experimento/katas"
RQ1_DIR="$REPO_ROOT/rq1-rq2-tempo-e-testes"
RQ3_TRIALS_DIR="$REPO_ROOT/rq3-metricas-estaticas/trials"
RESULTS_CSV="$RQ1_DIR/output/results.csv"
LOGS_DIR="$RQ1_DIR/output/logs"

# kata_id | pasta do kata | trial_num | tratamento   (DESIGN.md §7, linha do Pedro)
TRIALS=(
  "kata3|kata3-calorias|1|manual"
  "kata4|kata4-gastos|2|ia"
  "kata1|kata1-cinema|3|manual"
  "kata2|kata2-torneio-luta|4|ia"
)

PROBLEMAS=0
AVISOS=0

ok()      { echo "  [ OK ] $1"; }
falha()   { echo "  [FALHA] $1"; PROBLEMAS=$((PROBLEMAS + 1)); }
aviso()   { echo "  [aviso] $1"; AVISOS=$((AVISOS + 1)); }
info()    { echo "  [info] $1"; }

# Caminhos na saida ficam relativos a raiz do repo (absolutos poluem o relatorio).
rel() { echo "${1#"$REPO_ROOT"/}"; }

# Lê o valor de uma coluna (por nome) da linha do trial no results.csv.
coluna() {
  local linha="$1" nome="$2"
  local cabecalho
  cabecalho="$(head -n 1 "$RESULTS_CSV")"
  python - "$cabecalho" "$linha" "$nome" <<'PY'
import csv, io, sys
cab, linha, nome = sys.argv[1], sys.argv[2], sys.argv[3]
campos = next(csv.reader(io.StringIO(cab)))
valores = next(csv.reader(io.StringIO(linha)))
d = dict(zip(campos, valores))
print(d.get(nome, ""))
PY
}

echo "================================================================"
echo " Verificação dos trials do $MEMBER"
echo "================================================================"

[ -d "$KATAS_DIR" ] || { echo "ERRO: rode este script na raiz do repositório."; exit 1; }

if [ ! -f "$RESULTS_CSV" ]; then
  echo
  echo "ERRO: $RESULTS_CSV não existe — nenhum trial foi executado ainda."
  exit 1
fi

for t in "${TRIALS[@]}"; do
  IFS='|' read -r kata_id kata_pasta trial_num tratamento <<< "$t"
  echo
  echo "--- Trial $trial_num — $kata_pasta ($tratamento) ---"

  linha="$(grep ",${MEMBER},${kata_id},${trial_num}," "$RESULTS_CSV" | tail -n 1)"

  if [ -z "$linha" ]; then
    info "ainda não executado"
    continue
  fi

  n_linhas="$(grep -c ",${MEMBER},${kata_id},${trial_num}," "$RESULTS_CSV")"
  [ "$n_linhas" -gt 1 ] && aviso "há $n_linhas linhas para este trial no results.csv — conferir duplicata"

  # --- conferências do results.csv ---
  v_tratamento="$(coluna "$linha" treatment)"
  v_testcmd="$(coluna "$linha" test_cmd)"
  v_timebox="$(coluna "$linha" timebox_min)"
  v_total="$(coluna "$linha" tests_total)"
  v_passing="$(coluna "$linha" tests_passing)"
  v_censored="$(coluna "$linha" censored)"
  v_aborted="$(coluna "$linha" aborted)"
  v_prompts="$(coluna "$linha" prompts)"
  v_tempo="$(coluna "$linha" time_to_green_mmss)"

  [ "$v_tratamento" = "$tratamento" ] \
    && ok "tratamento = $tratamento" \
    || falha "tratamento gravado como '$v_tratamento', esperado '$tratamento'"

  case "$v_testcmd" in
    *-q*) falha "test_cmd = '$v_testcmd' — o -q impede a leitura dos testes; trial comprometido" ;;
    *)    ok "test_cmd = $v_testcmd" ;;
  esac

  [ "$v_timebox" = "35" ] \
    && ok "timebox = 35 min" \
    || falha "timebox = $v_timebox (esperado 35)"

  if [ "$v_total" = "10" ]; then
    ok "tests_total = 10 (leitura do Surefire funcionou)"
  elif [ "$v_total" = "0" ]; then
    falha "tests_total = 0 — o TrialTimer não leu a saída do Maven. Este trial NÃO mediu nada."
  else
    falha "tests_total = $v_total (esperado 10)"
  fi

  [ "$v_aborted" = "false" ] \
    && ok "aborted = false" \
    || falha "aborted = true — precisa de justificativa escrita no relatório"

  if [ "$v_censored" = "true" ]; then
    aviso "censored = true em $v_tempo — dado VÁLIDO (DESIGN.md §3), mas confirme que os 35 min estouraram de verdade e não foi erro de ambiente"
  else
    ok "green em $v_tempo ($v_passing/$v_total testes)"
  fi

  if [ "$tratamento" = "ia" ]; then
    [ "${v_prompts:-0}" -gt 0 ] 2>/dev/null \
      && ok "prompts = $v_prompts" \
      || falha "prompts = $v_prompts num trial 'ia' — a tecla 'p' não foi usada"
  else
    [ "${v_prompts:-0}" = "0" ] \
      && ok "prompts = 0 (correto no tratamento manual)" \
      || falha "prompts = $v_prompts num trial 'manual' — não deveria haver interação com IA"
  fi

  # --- log JSON ---
  n_logs="$(find "$LOGS_DIR" -name "${kata_id}_t${trial_num}_${tratamento}_*.json" 2>/dev/null | wc -l)"
  [ "$n_logs" -ge 1 ] \
    && ok "log JSON presente ($n_logs)" \
    || falha "nenhum log JSON em output/logs/ para ${kata_id}_t${trial_num}_${tratamento}"

  # --- código final na RQ3 ---
  destino="$RQ3_TRIALS_DIR/${MEMBER}-${kata_pasta}-${tratamento}"
  if [ -d "$destino" ]; then
    ok "código final em trials/${MEMBER}-${kata_pasta}-${tratamento}/"

    # O target/ reaparece sozinho: o language server do Java (redhat.java) compila
    # qualquer pom.xml que enxergue no workspace aberto. So e problema se for
    # entrar no commit — com a regra do .gitignore, nao e.
    if [ ! -d "$destino/target" ]; then
      ok "sem target/"
    elif git -C "$REPO_ROOT" check-ignore -q "$destino/target/" 2>/dev/null; then
      info "target/ existe (recriado pelo language server do Java), mas esta no .gitignore — nao vai para o commit"
    else
      falha "$(rel "$destino")/target/ existe e NAO esta ignorado pelo git — nao pode ir para o commit"
    fi

    # Regra de ouro 3: os arquivos de teste são imutáveis.
    if diff -r "$KATAS_DIR/$kata_pasta/src/test" "$destino/src/test" >/dev/null 2>&1; then
      ok "testes de aceitação intactos"
    else
      falha "os testes em $(rel "$destino")/src/test DIFEREM do kata original — trial invalidado"
      diff -rq "$KATAS_DIR/$kata_pasta/src/test" "$destino/src/test" 2>&1         | sed "s|$REPO_ROOT/||g; s/^/         /" | head -n 10
    fi

    n_java_main="$(find "$destino/src/main" -name '*.java' 2>/dev/null | wc -l)"
    [ "$n_java_main" -ge 1 ] \
      && ok "$n_java_main arquivo(s) .java em src/main" \
      || falha "nenhum .java em $(rel "$destino")/src/main"
  else
    falha "pasta $(rel "$destino") não existe — o código final não foi copiado"
  fi
done

# --- conferências globais ---
echo
echo "--- Global ---"

n_trials="$(grep -c ",${MEMBER}," "$RESULTS_CSV" 2>/dev/null || echo 0)"
echo "  Trials do $MEMBER em results.csv: $n_trials de 4"

if git -C "$REPO_ROOT" check-ignore -q "$RESULTS_CSV" 2>/dev/null; then
  falha "results.csv está sendo IGNORADO pelo git — é dado bruto versionado (DESIGN.md §13). Acrescente ao .gitignore, DEPOIS da regra que ignora output/: '!rq1-rq2-tempo-e-testes/output/results.csv' e '!rq1-rq2-tempo-e-testes/output/logs/'"
else
  ok "results.csv é versionável pelo git"
fi

if [ -d "$LOGS_DIR" ] && git -C "$REPO_ROOT" check-ignore -q "$LOGS_DIR" 2>/dev/null; then
  falha "output/logs/ está sendo IGNORADO pelo git — corrija o .gitignore"
else
  ok "output/logs/ é versionável pelo git"
fi

# Casa o PLACEHOLDER original da §11, não a palavra solta: depois de preenchida,
# a expressão "a definir" ainda aparece no texto do changelog da §14.
if grep -q "a definir pelo trio" "$REPO_ROOT/desenho-experimento/DESIGN.md" 2>/dev/null; then
  aviso "DESIGN.md §11 ainda tem 'a definir' — registre ferramenta, interface e versão do assistente antes do primeiro trial; depois disso o documento é congelado (§14)"
else
  ok "DESIGN.md §11 preenchido"
fi

echo
echo "================================================================"
if [ "$PROBLEMAS" -eq 0 ] && [ "$n_trials" -eq 4 ]; then
  echo " TUDO OK — $AVISOS aviso(s). Pronto para commitar."
  echo "================================================================"
  exit 0
elif [ "$PROBLEMAS" -eq 0 ]; then
  echo " Sem problemas nos trials executados ($n_trials/4) — $AVISOS aviso(s)."
  echo "================================================================"
  exit 0
else
  echo " $PROBLEMAS problema(s) e $AVISOS aviso(s) — veja acima."
  echo "================================================================"
  exit 1
fi
