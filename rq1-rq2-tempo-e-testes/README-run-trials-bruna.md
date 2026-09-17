# run_trials_bruna.sh — Roteiro guiado dos trials (S02)

Script que conduz a Bruna pelos 4 trials do experimento, na ordem e nos
tratamentos definidos em `desenho-experimento/DESIGN.md` (seção 7):

| Trial | Kata | Tratamento |
|---|---|---|
| 1 | kata1-cinema | ia |
| 2 | kata2-torneio-luta | manual |
| 3 | kata3-calorias | ia |
| 4 | kata4-gastos | manual |

O script **não codifica nada por você** — ele só automatiza a parte
mecânica (copiar o kata, aquecer o Maven, montar o comando do
`TrialTimer`, copiar o código final para a RQ3) para reduzir erro
manual e ganhar tempo dentro do time-box de 35 min.

## Onde ele deve estar

```
ia-vs-codificacao-manual/
└── rq1-rq2-tempo-e-testes/
    └── scripts/
        ├── TrialTimer.java
        └── run_trials_bruna.sh   ← este arquivo
```

Os caminhos internos do script (`desenho-experimento/katas`,
`rq1-rq2-tempo-e-testes`, `rq3-metricas-estaticas/trials`) são
**relativos à raiz do repositório**. Se você mover o script de lugar,
ajuste as variáveis `KATAS_DIR`, `RQ1_DIR` e `RQ3_TRIALS_DIR` no topo
do arquivo.

## Requisitos

- **JDK 17+** e **Maven** no `PATH` (`java -version`, `mvn -version`)
- Bash (Linux/macOS nativo; no Windows, use **Git Bash** ou **WSL** —
  o script não roda no PowerShell/CMD puro)
- Repositório clonado, com `TrialTimer.java` já compilado
  (`javac scripts/TrialTimer.java` dentro de `rq1-rq2-tempo-e-testes/`,
  ver o README dessa pasta)

## Como rodar

Sempre a partir da **raiz do repositório** (a pasta que contém
`desenho-experimento/`, `rq1-rq2-tempo-e-testes/` e
`rq3-metricas-estaticas/`):

```bash
chmod +x rq1-rq2-tempo-e-testes/scripts/run_trials_bruna.sh
./rq1-rq2-tempo-e-testes/scripts/run_trials_bruna.sh
```

O script vai:

1. Verificar se `mvn`/`java` existem e se `desenho-experimento/katas`
   existe (confirma que você está na raiz certa)
2. Mostrar a ordem dos 4 trials e pedir confirmação para começar
3. Para **cada trial**:
   - Se for `manual`: pausa e pede para você confirmar que já
     desabilitou o assistente de IA na IDE
   - Copia o kata para uma pasta temporária **fora do repositório**
     (em `$TMPDIR/lab02-trials-bruna/...`, ou `/tmp/...` se `$TMPDIR`
     não estiver definido) — isso evita o risco de vazamento descrito
     na ameaça à validade #2 do `DESIGN.md`
   - Roda `mvn -B test-compile` **fora do cronômetro**, para o
     download de dependências não contaminar o tempo medido
   - Pausa para você ler o `ENUNCIADO.md` do kata
   - Inicia o `TrialTimer` já com `--member bruna`, `--kata`,
     `--trial`, `--treatment` e `--test-cmd "mvn -B test"` corretos —
     você só interage com o timer normalmente (`p` para prompt, `s`
     para status, `q` para abortar)
   - Ao encerrar o trial (green ou fim do time-box), copia o código
     final para
     `rq3-metricas-estaticas/trials/bruna-<kata>-<tratamento>/`
4. Ao final dos 4 trials, lembra os passos manuais que faltam

## Depois de rodar

O script **não commita nada** — isso fica com você:

```bash
git status   # deve mostrar:
             #  - novas linhas em rq1-rq2-tempo-e-testes/output/results.csv
             #  - 4 pastas novas em rq3-metricas-estaticas/trials/bruna-*

git add rq1-rq2-tempo-e-testes/output/results.csv
git add rq3-metricas-estaticas/trials/bruna-*
git commit -m "feat(s02): trials da Bruna (kata1..kata4)"
git push
```

Confira antes de commitar:

- [ ] As 4 linhas em `results.csv` têm `member=bruna` e os tratamentos
      corretos (`ia, manual, ia, manual`)
- [ ] Nenhuma pasta em `rq3-metricas-estaticas/trials/bruna-*` contém
      `target/` do Maven (se tiver, apague antes de commitar — é
      artefato de build, não código)
- [ ] Se algum trial ficou `censored=true` no log, isso é esperado e
      **não deve ser descartado** — é dado válido para a RQ1

## Se algo der errado no meio do roteiro

O script usa `set -e`: se um comando falhar (ex.: `mvn test-compile`
sem internet na primeira vez), ele para naquele trial. Corrija o
problema e rode o script de novo — ele **sobrescreve** a pasta de
trabalho do trial (`rm -rf` antes de copiar), então é seguro re-rodar
um trial que ainda não foi commitado. Trials já concluídos e commitados
não devem ser re-executados (viola a ameaça #7 do `DESIGN.md` —
contaminação entre tratamentos).
