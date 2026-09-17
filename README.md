# ia-vs-codificacao-manual

Laboratório de experimentação: **assistentes de IA vs. codificação manual**
na resolução de katas. O repositório está organizado por questão de pesquisa
(RQ), cada uma com seu próprio ambiente, script e README:

- [`desenho-experimento/`](desenho-experimento/README.md) — **objeto experimental e desenho**:
  os 4 katas autorais em Java (enunciado, projeto Maven e testes de aceitação), o
  `check_katas.py` que os valida, e o [`DESIGN.md`](desenho-experimento/DESIGN.md) com
  hipóteses, variáveis, contrabalanceamento e ameaças à validade (Passo 1 do roteiro).
- [`rq1-rq2-tempo-e-testes/`](rq1-rq2-tempo-e-testes/README.md) — **RQ1** (time-to-green)
  e **RQ2** (taxa de testes passando), coletadas em tempo real durante o trial
  com o `TrialTimer`.
- [`rq3-metricas-estaticas/`](rq3-metricas-estaticas/README.md) — **RQ3**
  (complexidade ciclomática média/WMC via CK, % de linhas duplicadas via PMD
  CPD, e LOC como métrica de controle), coletadas sobre o código final de
  cada trial.

Cada pasta tem seu próprio `scripts/` (código versionado) e `output/`
(resultados — o que é reprodutível fica fora do git; CSVs de exemplo/consolidados
ficam versionados). Veja o README de cada RQ para requisitos, setup e uso.
