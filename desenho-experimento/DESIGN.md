# DESIGN.md — Desenho do Experimento

**Laboratório 02 · Assistentes de IA vs. codificação manual**
Engenharia de Software · Laboratório de Experimentação de Software · 6º período

> Documento correspondente ao **Passo 1 — Desenho do Experimento** do roteiro, e à Parte 3 da
> [Issue #3](https://github.com/Brunamark/ia-vs-codificacao-manual/issues/3).
> Define o experimento **antes** da coleta: nenhuma decisão desta página pode ser alterada
> depois que o primeiro trial da S02 começar, sob pena de invalidar a análise.

---

## 1. Objetivo (GQM · Goal)

Analisar **o uso de assistentes de IA generativa na resolução de tarefas de programação**,
com o propósito de **comparar seu efeito frente à codificação manual**,
com respeito a **tempo de resolução, qualidade funcional (defeitos) e qualidade estrutural do
código produzido**,
do ponto de vista **do grupo pesquisador**,
no contexto de **katas de dificuldade equivalente resolvidos por estudantes de graduação sob
condições controladas (crossover within-subject, time-boxed)**.

### Questões de pesquisa

| RQ | Pergunta |
|---|---|
| **RQ1** | O uso de assistente de IA reduz o tempo necessário para resolver uma tarefa de programação? |
| **RQ2** | O uso de assistente de IA reduz a quantidade de defeitos (testes que falham) no código produzido? |
| **RQ3** | O uso de assistente de IA altera a complexidade ciclomática ou a duplicação do código produzido? |

---

## 2. (A) Hipóteses

Todas as hipóteses nulas são formuladas de forma **bicaudal**: embora a expectativa informal
seja de que a IA reduza o tempo, não há evidência prévia suficiente para justificar um teste
unicaudal, e um efeito na direção oposta (IA atrapalha) é um resultado igualmente publicável.

| RQ | Hipótese nula (H0) | Hipótese alternativa (H1) |
|---|---|---|
| **RQ1** | H0₁: a mediana do *time-to-green* é igual entre os tratamentos `ia` e `manual` | H1₁: as medianas diferem |
| **RQ2** | H0₂: a mediana da taxa de testes de aceitação passando ao fim do time-box é igual entre os tratamentos | H1₂: as medianas diferem |
| **RQ3a** | H0₃ₐ: a mediana do WMC médio por método é igual entre os tratamentos | H1₃ₐ: as medianas diferem |
| **RQ3b** | H0₃♭: a mediana da % de linhas duplicadas é igual entre os tratamentos | H1₃♭: as medianas diferem |
| **RQ3c** | H0₃꜀: a mediana do LOC é igual entre os tratamentos | H1₃꜀: as medianas diferem |

RQ3 é desdobrada em três hipóteses porque envolve três variáveis distintas. **LOC é métrica de
controle**, não de resultado: ela existe para interpretar RQ3a e RQ3b — se o código gerado com
IA for sistematicamente mais longo, uma diferença bruta em complexidade ou duplicação pode ser
efeito do tamanho, não do tratamento.

**Nível de significância adotado: α = 0,05** para todas as hipóteses.

---

## 3. (B) Variáveis dependentes

Todas as variáveis são extraídas **automaticamente** dos CSVs produzidos pelos scripts do
grupo. Nenhuma medição é feita à mão, o que elimina erro de transcrição e viés do observador.

| Variável | Unidade | Origem | RQ |
|---|---|---|---|
| `time_to_green_sec` | segundos (censurado em 2100) | `TrialTimer` → `output/results.csv` | RQ1 |
| `test_success_rate` | proporção 0–1 | `TrialTimer` → `output/results.csv` | RQ2 |
| `tests_failures` | contagem | `TrialTimer` → `output/results.csv` | RQ2 (complementar) |
| `wmc_medio_por_metodo` | média de McCabe por método | `collect_metrics.py` → `output/metrics.csv` | RQ3a |
| `duplicacao_percentual` | % | `collect_metrics.py` → `output/metrics.csv` | RQ3b |
| `loc` | linhas físicas | `collect_metrics.py` → `output/metrics.csv` | RQ3c (controle) |
| `prompts` | contagem | `TrialTimer` (tecla `p`) | exploratória |

**Tratamento da censura.** Um trial que atinge o time-box de 35 min sem passar em todos os
testes é registrado como `time_to_green_sec = 2100` com `censored = true`, **nunca descartado**.
Descartar falhas distorceria a comparação a favor do tratamento com mais falhas. Trials
censurados entram na análise da RQ1 com o valor do teto; a quantidade de censuras por
tratamento é reportada separadamente, porque um teto comum reduz a diferença observável entre
os grupos (efeito conservador — dificulta rejeitar H0, não facilita).

---

## 4. (C) Variável independente

**Uso do assistente de IA** — fator único, categórico, de dois níveis: `ia` e `manual`.

---

## 5. (D) Tratamentos

O mesmo assistente de IA é usado por todos os integrantes em todos os trials do nível `ia`,
para que o tratamento seja comparável dentro do experimento. Ferramenta e versão exatas são
registradas na seção [Instrumentação](#11-instrumentação).

### Tratamento `ia` — com assistente

- Assistente habilitado na IDE e/ou chatbot da mesma ferramenta, à vontade do participante.
- Permitido: qualquer prompt, autocompletar, geração de código, explicação, refatoração.
- Cada interação com o assistente é registrada pressionando `p` no `TrialTimer`
  (métrica exploratória `prompts`).

### Tratamento `manual` — sem assistente

- Assistente **desabilitado na IDE antes de abrir o projeto** (não basta não usar: o
  autocompletar inline age sem ser chamado).
- Permitido: documentação oficial da linguagem/JDK e da API de testes.
- Proibido: qualquer ferramenta generativa, busca por solução pronta do problema, Stack
  Overflow, fóruns.

### Regras comuns aos dois tratamentos

1. Time-box de **35 min por trial**, encerrado independentemente do resultado.
2. Os arquivos de teste são **imutáveis** — alterar um teste invalida o trial. A integridade é
   verificada por hash (`MANIFEST.sha256`) ao final.
3. O trial roda em uma **cópia do esqueleto fora deste repositório** (ver
   [ameaça de vazamento](#10-h-ameaças-à-validade)).
4. O cache Maven (`~/.m2`) é aquecido antes de iniciar o cronômetro, para que download de
   dependência não entre no tempo medido.

---

## 6. (E) Objetos experimentais

**Quatro katas autorais em Java**, escritos pelo grupo e inéditos até a publicação deste
repositório:

| # | Kata | Método público | Núcleo algorítmico |
|---|---|---|---|
| K1 | Bilheteria do Cinema | `int totalCentavos(List<String>)` | tabela de preços por sessão + meia-entrada não cumulativa + adicional fora do cálculo do desconto |
| K2 | Torneio de Jogos de Luta | `List<String> classificacao(List<String>)` | agregação de pontos + ordenação com três critérios de desempate |
| K3 | Contador de Calorias | `List<String> diasAcimaDaMeta(List<String>)` | agregação por dia com valores de sinal oposto + saturação em zero + filtro por limiar |
| K4 | Organizador de Gastos | `int saldoCentavos(List<String>)` | soma com sinal + divisão de parcela com arredondamento para cima + descarte de registro inválido |

Os quatro compartilham a forma algorítmica descrita abaixo, mas se distribuem em **dois pares
estruturais** — K1 e K4 devolvem um escalar, K2 e K3 devolvem lista ordenada — e, dentro de
cada par, a operação característica difere (tabela de preços vs. soma com sinal; ranking com
desempate vs. filtro cronológico por limiar). Isso mantém a dificuldade equivalente sem que
resolver um kata ensine a solução do seguinte (ver ameaça 3).

**Por que 4 e não 6.** O roteiro permite ambos (número par, para divisão exata entre
tratamentos). Com 4 katas e 3 integrantes são 12 trials, ~2h20 de time-box por pessoa, o que
cabe na janela da S02. A seção [9.3](#93-poder-estatístico) mostra que 4 katas ainda permitem
que o teste de Wilcoxon atinja significância — que é a razão real de se querer 6.

**Por que autorais.** É a mitigação direta da ameaça de memorização: um exercício clássico
(FizzBuzz, números romanos, boliche) pode ser reproduzido de memória pelo assistente a partir
do treinamento, o que mediria recuperação de solução vista, não auxílio à resolução.

**Domínio comum, regras inéditas.** Dois dos domínios escolhidos (contagem de calorias e
controle de gastos) são temas frequentes de tutoriais e apps de exemplo. Isso não é problema
por si: a memorização se prende ao **enunciado e aos testes exatos**, não ao assunto. O risco
real é outro e mais sutil — se as regras forem as *óbvias* do domínio, o assistente acerta sem
precisar ler a especificação, o que é uma forma de vazamento. Por isso cada kata carrega ao
menos uma regra deliberadamente contraintuitiva (meia-entrada que não acumula; treino que
subtrai calorias e satura em zero; `parcelas` ignorado em receitas), que só se acerta lendo o
`ENUNCIADO.md`.

### Critério de dificuldade equivalente

Os quatro katas compartilham deliberadamente **a mesma forma algorítmica**:

> recebe `List<String>` de registros separados por `;` → *parsing* → aplica uma tabela de
> regras de negócio → agrega (soma ou ordena) → devolve o resultado.

Nenhum exige estrutura de dados além de `List`/`Map`, recursão, ou dependência externa além do
JUnit. A equivalência não é declarada, é **medida** — os valores abaixo são verificados pelo
`check_katas.py` e pelo `collect_metrics.py` sobre as soluções de referência, e o resultado é
versionado em `output/validacao-katas.csv`:

| Critério | Faixa-alvo | K1 | K2 | K3 | K4 |
|---|---|---:|---:|---:|---:|
| LOC da solução de referência | 45 – 65 | 48 | **72** | 53 | **41** |
| WMC médio por método | 2,5 – 4,5 | **2,33** | 2,67 | 4,00 | **2,33** |
| Duplicação interna | — | 0,0% | 0,0% | 0,0% | 0,0% |
| Métodos públicos | 1 (+ auxiliares privados) | 1 | 1 | 1 | 1 |
| Testes de aceitação | exatamente 10 por kata | 10 | 10 | 10 | 10 |
| Regras de negócio no enunciado | 4 | 4 | 4 | 4 | 4 |
| Dependências | somente JUnit 5 (`pom.xml` idêntico) | ✓ | ✓ | ✓ | ✓ |
| Tempo no trial-piloto | 15 – 20 min | — | — | — | — |

Medições de 2026-09-10 (CK `0.7.1-SNAPSHOT` commit `8a1ef91`; PMD 7.27.0 com
`--min-tokens 50`), versionadas em `output/metricas-referencia.csv`. Valores **em negrito**
estão fora da faixa registrada antes da implementação.

**Desvio declarado.** O K2 (72 LOC) é o kata mais pesado e o K4 (41 LOC) o mais leve — uma
dispersão de 1,76×, que não é estilo de escrita: o K2 é o único que exige modelar uma entidade
com três acumuladores e ordenar por quatro critérios, enquanto o K4 é uma soma sem estado. A
faixa **não foi alargada para acomodar o resultado**. O desvio é tratado como limitação, com
três mitigações: (i) o contrabalanceamento distribui cada kata entre os dois tratamentos, de
modo que um kata mais pesado não favorece sistematicamente nenhum deles; (ii) os quatro têm a
mesma forma algorítmica, o mesmo número de regras (4) e de testes (10); (iii) o critério
decisivo é o tempo do trial-piloto, ainda pendente — se ele confirmar diferença material, o
terceiro desempate do K2 (*perfect*) é removido antes da S02.

---

## 7. (F) Tipo de projeto experimental

**Crossover within-subject contrabalanceado.**

Cada integrante resolve **todos os 4 katas**, metade com IA e metade sem. O desenho
within-subject faz de cada participante seu próprio controle, o que neutraliza a variação
individual de habilidade — a maior fonte de ruído em experimentos com N pequeno de pessoas.

O contrabalanceamento age em dois eixos simultaneamente:

- **Ordem dos katas** difere entre integrantes → controla efeito de aprendizado e fadiga.
- **Tratamento por kata** difere entre integrantes → controla dificuldade residual de cada kata.

| Integrante | 1º trial | 2º trial | 3º trial | 4º trial |
|---|---|---|---|---|
| Bruna | K1 · **IA** | K2 · manual | K3 · **IA** | K4 · manual |
| Pedro | K3 · manual | K4 · **IA** | K1 · manual | K2 · **IA** |
| Thiago | K2 · **IA** | K1 · manual | K4 · manual | K3 · **IA** |

**Balanceamento obtido:**

- 6 trials `ia` e 6 trials `manual` no total;
- cada integrante: exatamente 2 e 2;
- cada kata aparece em posições diferentes da sequência entre os integrantes;
- por kata: K1 e K4 saem 1×`ia`/2×`manual`; K2 e K3 saem 2×`ia`/1×`manual`.

O desbalanceamento residual por kata é **inevitável com 3 integrantes** (número ímpar não se
divide igualmente em dois tratamentos por kata) e está declarado como limitação. Ele é
parcialmente compensado pelo fato de os katas terem dificuldade equivalente verificada
(seção 6) e pelo pareamento intra-integrante da análise (seção 9.2).

---

## 8. (G) Quantidade de medições

| Item | Quantidade |
|---|---|
| Integrantes (sujeitos) | 3 |
| Katas (objetos) | 4 |
| Trials do experimento | **12** (3 × 4) |
| Trials por tratamento | 6 `ia` + 6 `manual` |
| Variáveis dependentes por trial | 5 (+1 exploratória) |
| **Medições totais** | **60** (12 × 5), mais 12 registros de `prompts` |
| Pares para o teste estatístico | **6** (ver 9.2) |
| Trials-piloto | 4 (1 por kata) — **fora da análise**, servem só para calibrar dificuldade |

Cada trial gera ainda: 1 linha em `results.csv`, 1 log JSON detalhado, e 1 linha em
`metrics.csv` após a coleta estática sobre o código final.

---

## 9. Análise estatística

### 9.1 Métricas escolhidas por RQ e justificativa

O roteiro apresenta métricas candidatas; a escolha do grupo e o motivo:

| RQ | Métrica escolhida | Justificativa da escolha |
|---|---|---|
| **RQ1** | *time-to-green*, censurado em 2100 s | Métrica primária recomendada pelo roteiro; é a que traduz diretamente "tempo para resolver a tarefa", e não uma proxy |
| RQ1 | agregação por **mediana** | N pequeno (4–6 trials por tratamento por pessoa) e censura criam um teto artificial em 2100 s que arrastaria a média; a mediana é insensível a esse teto enquanto menos da metade dos trials for censurada |
| RQ1 | nº de prompts *(exploratória)* | Coletada sem custo pelo `TrialTimer`; usada apenas na discussão qualitativa, sem teste de hipótese |
| **RQ2** | **taxa de sucesso** (% de testes passando) | Mais robusta que a contagem bruta por normalizar katas com números diferentes de testes. Aqui todos têm 10, mas a taxa mantém a análise válida caso um kata precise de ajuste após o piloto |
| RQ2 | nº absoluto de testes falhando *(complementar)* | Mais legível no relatório e nos gráficos; reportado junto, não testado separadamente |
| **RQ3a** | **WMC médio por método** (McCabe, via CK) | Métrica pedida pelo roteiro; a média por método (e não a soma por classe) permite comparar trials com números diferentes de métodos |
| **RQ3b** | **% de linhas duplicadas** (PMD CPD) | Métrica pedida pelo roteiro; o percentual, e não a contagem bruta, já normaliza pelo tamanho do trial |
| **RQ3c** | **LOC** — métrica de controle | **Obrigatória** pelo roteiro sempre que se reporta complexidade ou duplicação: código gerado por IA tende a ser mais verboso, e uma diferença em WMC ou duplicação sem controlar por LOC pode ser puro efeito de tamanho |

Estatística descritiva de todas as variáveis: **mediana e IQR** (intervalo interquartil), não
média e desvio-padrão, conforme orientação de robustez do roteiro para amostras pequenas.

### 9.2 Teste inferencial e esquema de pareamento

**Teste de Wilcoxon pareado (signed-rank), bicaudal, α = 0,05** — não paramétrico, adequado ao
desenho within-subject e a amostras pequenas sem suposição de normalidade.

O ponto delicado é **o que forma um par**. A escolha do grupo: **pares intra-integrante entre
trials consecutivos**. Cada integrante contribui com 2 pares — os trials nas posições 1–2 e nas
posições 3–4 da sua sequência, que por construção contêm um trial de cada tratamento:

| Par | Integrante | Trial `ia` | Trial `manual` |
|---|---|---|---|
| P1 | Bruna | K1 (pos. 1) | K2 (pos. 2) |
| P2 | Bruna | K3 (pos. 3) | K4 (pos. 4) |
| P3 | Pedro | K4 (pos. 2) | K3 (pos. 1) |
| P4 | Pedro | K2 (pos. 4) | K1 (pos. 3) |
| P5 | Thiago | K2 (pos. 1) | K1 (pos. 2) |
| P6 | Thiago | K3 (pos. 4) | K4 (pos. 3) |

**n = 6 pares.** Parear trials *consecutivos* (e não quaisquer dois do mesmo integrante) tem
uma vantagem adicional: dentro de um par, fadiga, horário e ambiente são praticamente os
mesmos, então a diferença observada fica mais limpa.

O mesmo esquema de pareamento é aplicado às cinco variáveis dependentes (RQ1, RQ2 e as três da
RQ3), para que os resultados sejam comparáveis entre as questões.

**Tratamento de empates.** Diferenças nulas (ex.: dois trials com 100% dos testes passando) são
tratadas pelo método padrão do `scipy.stats.wilcoxon` e o número de empates é reportado. Caso
todos os 6 pares empatem em alguma variável — cenário plausível na RQ2 se todos os trials
ficarem *green* — o teste é indefinido e o resultado será reportado descritivamente, sem p-valor.

**Tamanho de efeito.** Junto do p-valor será reportada a correlação bisserial de postos pareada
(*matched-pairs rank-biserial correlation*), porque com n = 6 o p-valor sozinho diz pouco sobre
a magnitude do efeito.

### 9.3 Poder estatístico

Esta subseção justifica quantitativamente a escolha de 4 katas e do esquema de pareamento.

Se o pareamento fosse **por integrante** (mediana `ia` vs. mediana `manual` de cada pessoa),
teríamos n = 3 pares. O Wilcoxon pareado com n = 3 tem **p mínimo de 0,25** bicaudal: seria
matematicamente impossível rejeitar H0, qualquer que fosse o resultado observado. Um
experimento assim já nasce sem poder de decisão.

Com o pareamento intra-integrante da seção 9.2, **n = 6** e o p mínimo do Wilcoxon cai para
**0,031** bicaudal — abaixo de α = 0,05. O teste passa a ser capaz de rejeitar H0 se todos os
6 pares apontarem na mesma direção.

Isso deixa explícito o limite do experimento: **só um efeito perfeitamente consistente (6/6
pares na mesma direção) atinge significância.** Um efeito real mas moderado não será detectado.
Essa é uma limitação de poder assumida conscientemente, e um resultado não significativo será
reportado como "não foi possível rejeitar H0 com este N" — nunca como "não há efeito".

---

## 10. (H) Ameaças à validade

| # | Ameaça | Tipo | Mitigação adotada |
|---|---|---|---|
| 1 | **Memorização** — o assistente reproduz solução vista no treinamento em vez de resolver | Interna | Katas autorais e inéditos, com domínio e regras inventados pelo grupo |
| 2 | **Vazamento pelo repositório** — a solução de referência vive no mesmo repositório público que o participante abriria na IDE, e assistentes leem arquivos abertos e vizinhos como contexto | Interna | Trials rodam em **cópia do esqueleto fora do repositório**; a pasta `solucoes-referencia/` nunca é aberta durante um trial; regra fixada no protocolo de execução |
| 3 | **Efeito de aprendizado** — o 4º kata é resolvido com mais prática que o 1º | Interna | Ordem dos katas contrabalanceada entre integrantes (seção 7) |
| 4 | **Dificuldade residual desigual** entre katas — **confirmada em parte**: o K2 tem 72 LOC de referência contra 41 do K4 | Interna | Mesma forma algorítmica, mesmo nº de regras e de testes; contrabalanceamento distribui cada kata entre os dois tratamentos; trial-piloto decide se o K2 precisa ser simplificado (seção 6) |
| 5 | **Variação individual de habilidade** | Interna | Desenho within-subject: cada integrante é seu próprio controle |
| 6 | **Familiaridade prévia desigual com a ferramenta de IA** | Interna | Mesmo assistente e mesma versão para os três; experiência prévia autodeclarada registrada no relatório |
| 7 | **Contaminação entre tratamentos** — lembrar da solução de um kata já resolvido | Interna | Cada kata é resolvido **uma única vez** por integrante; nenhum kata se repete entre tratamentos para a mesma pessoa |
| 8 | **Testes de aceitação enviesados** — foram escritos por nós, que também definimos o kata | Construto | Testes escritos **antes** das soluções de referência; `check_katas.py` exige que o esqueleto puro **falhe** (prova que os testes não são vazios); arquivos de teste imutáveis, verificados por hash |
| 9 | **`time-to-green` não captura qualidade** — passar nos testes não é o mesmo que bom código | Construto | É exatamente por isso que a RQ3 existe: as métricas estáticas cobrem a dimensão que o tempo ignora |
| 10 | **Censura em 35 min** achata a distribuição da RQ1 | Conclusão | Censura registrada explicitamente (`censored=true`); uso de mediana; katas calibrados no piloto para ~20 min, reduzindo a chance de censura generalizada. O efeito da censura é conservador |
| 11 | **Overhead de medição** — o `TrialTimer` executa a suíte a cada 5 s durante o trial | Conclusão | Custo idêntico nos dois tratamentos, logo não enviesa a comparação; a resolução de `time_to_green` fica limitada ao intervalo de *polling* somado ao tempo do Maven (~5–10 s) — erro sistemático declarado |
| 12 | **N pequeno** — 12 trials, 6 pares | Conclusão | Teste não paramétrico; limite de poder explicitado na seção 9.3; resultado não significativo reportado como tal |
| 13 | **Múltiplas comparações** — 5 hipóteses testadas sobre os mesmos dados | Conclusão | Nenhuma correção (Bonferroni etc.) é aplicada, dado o caráter exploratório e o poder já limitado; o risco de falso positivo é declarado no relatório |
| 14 | **Generalização** — 3 estudantes, 4 katas, uma linguagem, um assistente | Externa | Escopo declarado explicitamente; nenhuma extrapolação para "desenvolvimento profissional" ou "IA em geral" |
| 15 | **Experimentadores são os sujeitos** — o grupo conhece as hipóteses | Externa / Interna | Desenho, katas e critérios fixados **antes** do primeiro trial (este documento); métricas coletadas por script, sem julgamento humano |

> **Caso concreto da ameaça #15, declarado em 2026-09-17.** O integrante **Thiago é o autor dos
> 4 katas e das 4 soluções de referência** (commit `27e27b0`, 2026-09-10) e também é sujeito dos
> trials 1–4 da sua sequência. Ele não apenas conhece as hipóteses: **já resolveu os quatro
> problemas**. Isso encurta o tempo dele nos dois tratamentos, de forma assimétrica em relação
> aos outros dois integrantes.
>
> *Por que o dado ainda é utilizável:* o pareamento da §9.2 é **intra-integrante**, e o teste de
> Wilcoxon opera sobre a diferença *dentro* de cada par. O conhecimento prévio acelera tanto o
> trial `ia` quanto o `manual` do mesmo par, então o efeito se cancela em boa parte na
> diferença — que é exatamente a grandeza testada. O que ele contamina é o **nível** absoluto
> dos tempos do Thiago, não o sinal da diferença.
>
> *Mitigações adotadas:* (i) `solucoes-referencia/` não é reaberta antes nem durante os trials;
> (ii) os enunciados só são relidos no passo 5 do protocolo, fora do cronômetro; (iii) os tempos
> absolutos do Thiago não são comparados com os da Bruna e do Pedro na estatística descritiva
> sem essa ressalva; (iv) a limitação é reportada no relatório final.

---

## 11. Instrumentação

| Item | Ferramenta | Versão | Papel |
|---|---|---|---|
| Cronometragem e RQ1/RQ2 | `TrialTimer.java` | deste repositório | Mede *time-to-green*, taxa de testes, prompts |
| Métricas estáticas RQ3 | `collect_metrics.py` + CK + PMD CPD | CK `0.7.1-SNAPSHOT`; PMD 7.27.0 | WMC, duplicação, LOC |
| Validação dos katas | `check_katas.py` | deste repositório | Confirma que os katas são válidos como objeto experimental |
| Linguagem dos katas | Java | JDK 21.0.7 (Zulu); mínimo 17 | Exigência do CK e do PMD (Java-only) |
| Build e testes | Maven + Surefire + JUnit 5 | Maven 3.9.11; Surefire 3.2.5; JUnit 5.10.2 | Maven é necessário porque o Surefire imprime `Tests run: X, Failures: Y, Errors: Z`, padrão que o `TrialTimer` lê |
| Análise estatística | Python + Pandas + SciPy | S03 | Wilcoxon, mediana/IQR |
| Dashboard | Pandas + Matplotlib/Seaborn | S03 | Passo 6 do roteiro |
| **Assistente de IA** | Claude (Anthropic), pelo chat do **claude.ai** no navegador | **Sonnet 5** (`claude-sonnet-5`) | Tratamento `ia` — deve ser o mesmo em todos os trials |

> **Decidido pelo trio em 2026-09-17:** Claude (Anthropic) pelo chat do **claude.ai**,
> modelo **Sonnet 5**. Registrado aqui após o início da coleta — ver §14.
>
> **Consequência para o protocolo.** O claude.ai não tem acesso ao sistema de arquivos: o
> participante transfere enunciado, código e saída dos testes manualmente (copiar/colar). Isso
> tem dois efeitos declarados: (i) elimina por construção a ameaça #2 (vazamento pelo
> repositório) no tratamento `ia`, porque o assistente só vê o que for colado; (ii) inclui o
> custo de transferência **dentro** do time-box, que um assistente integrado à IDE não teria.
> O (ii) faz parte do tratamento medido — "usar um assistente pelo chat" — e não é confundidor
> em relação ao `manual`, mas limita a generalização: os resultados não se estendem a
> assistentes integrados à IDE (autocompletar inline, leitura automática de contexto).
> Registrar essa distinção ao discutir a ameaça #14 no relatório.
>
> ✅ **Uniformidade confirmada em 2026-09-17.** A Bruna executou os 4 trials dela com a mesma
> instrumentação — Claude pelo chat do claude.ai, modelo Sonnet 5. A
> [ameaça #6](#10-h-ameaças-à-validade) (familiaridade/ferramenta desigual) fica mitigada como
> previsto: mesma ferramenta, mesma interface e mesmo modelo nos 12 trials. Falta apenas o
> Pedro seguir o mesmo procedimento.

---

## 12. Procedimento de execução (S02)

Para cada um dos 12 trials, na ordem definida na tabela da seção 7:

1. Copiar `desenho-experimento/katas/<kata>/` para uma pasta de trabalho **fora deste repositório**.
2. No tratamento `manual`, desabilitar o assistente na IDE **antes** de abrir a pasta.
3. Abrir **somente** a pasta copiada na IDE — nunca o repositório inteiro.
4. Aquecer o Maven (`mvn -B test-compile`) fora do cronômetro.
5. Ler o `ENUNCIADO.md`; iniciar o `TrialTimer` e só então começar a codificar:
   ```bash
   # a partir de rq1-rq2-tempo-e-testes/
   java -cp scripts TrialTimer --member <nome> --kata <kataN> --trial <n> \
        --treatment <ia|manual> --test-cmd "mvn -B test" --cwd <pasta-de-trabalho>
   ```
6. No tratamento `ia`, pressionar `p` a cada interação com o assistente.
7. Ao encerrar (green ou time-box), copiar o código final para
   `rq3-metricas-estaticas/trials/<membro>-<kata>-<tratamento>/`.
8. Fechar a Issue individual daquele trial no GitHub Projects, referenciando o commit.

Após os 12 trials, rodar `collect_metrics.py` uma única vez sobre todos os trials para gerar o
`metrics.csv` consolidado.

---

## 13. Rastreabilidade

- Cada trial é uma **Issue individual** no GitHub Projects do grupo, com Assignee do integrante
  responsável (uma Issue por kata/tratamento).
- Todo commit referencia o número da Issue correspondente.
- Dados brutos versionados: `rq1-rq2-tempo-e-testes/output/results.csv`,
  `rq3-metricas-estaticas/output/metrics.csv` e os logs JSON por trial.

---

## 14. Alterações a este documento

Depois de iniciada a coleta (primeiro trial da S02), este documento é **congelado**. Qualquer
alteração posterior deve ser registrada abaixo, com data e motivo, e discutida no relatório
final como desvio de protocolo.

| Data | Alteração | Motivo |
|---|---|---|
| 2026-09-17 | §11 — preenchida a linha do assistente de IA: Claude via claude.ai, modelo Sonnet 5 | O campo estava como *a definir* e o documento previa que fosse fechado antes do primeiro trial da S02; a decisão do trio só saiu depois que a Bruna já havia executado os trials dela. Registrado como desvio de protocolo: a instrumentação do tratamento `ia` não estava fixada quando a coleta começou |
| 2026-09-17 | §11 — declarada a consequência do uso do chat (transferência manual dentro do time-box; limite de generalização para assistentes integrados à IDE) | A escolha da interface altera o que o tratamento `ia` de fato mede; precisa estar explícita antes da análise, não ser descoberta na discussão dos resultados |
| 2026-09-17 | §10 — nota acrescentada após a tabela de ameaças, declarando que o autor dos katas e das soluções de referência é também sujeito dos trials 1–4 da sequência do Thiago | A ameaça #15 cobria "experimentadores conhecem as hipóteses"; o caso concreto é mais forte e precisa ser nomeado |
| — | — | — |
