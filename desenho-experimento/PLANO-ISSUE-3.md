# Plano de execução — Issue #3 · Seleção dos katas + validação automatizada + DESIGN.md

> **Sprint:** Lab02S01 · **Assignee:** Thiago Branco · **Issue:** [#3](https://github.com/Brunamark/ia-vs-codificacao-manual/issues/3)
> **Objetivo do card:** entregar (1) o conjunto final de katas com justificativa de dificuldade
> equivalente, (2) um artefato de código versionado que valide esses katas automaticamente e
> (3) o `DESIGN.md` com o desenho completo do experimento (Passo 1 do roteiro).

Este documento é o **plano**: descreve o que será construído, onde, com que contrato e em que
ordem. Os artefatos em si (`DESIGN.md`, katas, `check_katas.py`) são produzidos nas tarefas
listadas na seção [Plano de commits](#8-plano-de-commits-e-tarefas).

---

## 1. Onde isso se encaixa no que já existe

O repositório hoje está organizado **por questão de pesquisa**, cada pasta com `scripts/`
(código versionado), `output/` (resultados) e um `README.md` próprio:

| Pasta | Dono | Entrega |
|---|---|---|
| [`rq1-rq2-tempo-e-testes/`](../rq1-rq2-tempo-e-testes/README.md) | Bruna | `TrialTimer.java` — time-to-green (RQ1) e taxa de testes passando (RQ2) |
| [`rq3-metricas-estaticas/`](../rq3-metricas-estaticas/README.md) | Pedro | `collect_metrics.py` — WMC médio, % duplicação e LOC via CK + PMD CPD (RQ3) |
| **`desenho-experimento/`** | **Thiago (este card)** | **objeto experimental (katas) + validação + desenho** |

As duas pastas existentes medem. **Falta o que é medido** — os katas — e o **documento que
define o experimento**. É exatamente esse o escopo da Issue #3, e por isso a nova pasta segue a
mesma convenção das outras duas (pasta própria, `scripts/`, `README.md`, artefatos versionados).

### Estrutura proposta

```
desenho-experimento/
├── README.md                     # setup, como rodar o check_katas, contrato dos katas
├── DESIGN.md                     # Parte 3 do card — desenho do experimento (A..H)
├── PLANO-ISSUE-3.md              # este documento
├── scripts/
│   └── check_katas.py            # Parte 2 — validação automatizada dos katas
├── katas/                        # Parte 1 — esqueletos Maven entregues aos participantes
│   ├── kata1-cinema/
│   │   ├── ENUNCIADO.md
│   │   ├── pom.xml
│   │   ├── src/main/java/kata/Bilheteria.java         # assinatura + TODO (esqueleto)
│   │   └── src/test/java/kata/BilheteriaTest.java     # testes de aceitação (imutáveis)
│   ├── kata2-torneio-luta/
│   ├── kata3-calorias/
│   └── kata4-gastos/
├── solucoes-referencia/          # NÃO entregue aos participantes — só para validação
│   ├── kata1-cinema/Bilheteria.java
│   ├── ...
│   └── MANIFEST.sha256           # hashes dos arquivos de teste (integridade)
└── output/
    └── validacao-katas.csv       # saída do check_katas (versionada — é evidência do card)
```

### Contratos com os artefatos dos colegas

Dois contratos precisam ser respeitados pelos katas, senão as ferramentas já prontas não
funcionam sobre eles:

1. **`TrialTimer` (Bruna)** — o `--test-cmd` precisa imprimir `Tests run: X, Failures: Y, Errors: Z`
   no stdout (regex em [`TrialTimer.java:57-59`](../rq1-rq2-tempo-e-testes/scripts/TrialTimer.java#L57-L59)),
   e o processo precisa sair com código 0 quando tudo passa. Isso é **Maven Surefire**.
   ⇒ os katas serão **projetos Maven** (Gradle não imprime esse padrão sem configuração extra).
2. **`collect_metrics.py` (Pedro)** — varre `.java` recursivamente sob `trials/<nome>/` e usa
   CK + PMD CPD, ambos Java-only. ⇒ os katas serão **em Java**, e o código final de cada trial
   será copiado para [`rq3-metricas-estaticas/trials/`](../rq3-metricas-estaticas/README.md) na S02.

> ✅ **Risco de integração CONFIRMADO e resolvido (T5, 2026-09-10).** O README da RQ1/RQ2
> sugere `--test-cmd "mvn -q test"`. Verificado empiricamente com Maven 3.9.11 e Surefire
> 3.2.5: com `-q`, **nem o sucesso nem a falha** imprimem `Tests run:` — no sucesso a saída é
> completamente vazia. Como o `TrialTimer` exige `r.run > 0` para declarar *green*, todo trial
> rodado assim seria censurado em 35 min e a RQ2 sairia zerada. Com `mvn -B test`, o padrão
> aparece corretamente nos dois casos (`Tests run: 10, Failures: 0` e `Tests run: 10,
> Failures: 5`), e o regex do `TrialTimer` extrai os valores certos — o cabeçalho solto
> `[ERROR] Failures:` não casa, por não ter dígitos.
> **Correção:** usar `mvn -B test` em todos os comandos do experimento. O código do
> `TrialTimer` está correto; é uma linha no README da Bruna. Tabela completa em
> [`README.md`](README.md#nota-de-integração-mvn--q-test-quebra-a-medição-da-rq1).

---

## 2. Parte 1 — Katas

### 2.1 Quantos e por quê

**4 katas.** O roteiro permite 4 ou 6 (número par, para dividir exatamente entre os
tratamentos). Com 4 katas e 3 integrantes temos **12 trials**, cada integrante fazendo
2 com IA e 2 sem — cerca de **2h20 de time-box por pessoa**. Com 6 katas seriam 18 trials e
3h30 por pessoa, o que não cabe com folga na janela da S02. A seção
[4.4](#44-poder-estatístico-por-que-4-katas-ainda-permitem-o-wilcoxon) mostra que 4 katas ainda
permitem que o teste de Wilcoxon alcance significância — que é o motivo real de se querer 6.

### 2.2 Critério de seleção

Os katas são **autorais** (escritos pelo grupo, nunca publicados antes), justamente para
atacar a ameaça de **memorização** apontada no roteiro: um exercício clássico de
LeetCode/HackerRank pode ser reproduzido de memória pelo assistente, o que mediria
"recuperação de solução vista" em vez de "ajuda na resolução".

Todos seguem **a mesma forma algorítmica**, o que é a base da equivalência de dificuldade:

> recebe `List<String>` de registros separados por `;` → faz *parsing* → aplica uma tabela de
> regras de negócio → agrega (soma ou ordena com critérios de desempate) → devolve o resultado.

Nenhum exige estrutura de dados além de `List`/`Map`, recursão, ou biblioteca externa
(além do JUnit).

**Domínio comum não é o problema — regra adivinhável é.** Dois dos domínios escolhidos
(calorias e gastos) são temas frequentes de tutorial. Isso é aceitável, porque a memorização se
prende ao enunciado e aos testes exatos, não ao assunto. O risco real é mais sutil: se as
regras forem as *óbvias* do domínio, o assistente acerta sem ler a especificação — vazamento
por outro caminho. Por isso **cada kata carrega ao menos uma regra deliberadamente
contraintuitiva**, destacada nas fichas abaixo, e dois dos dez testes de cada kata cobrem
exatamente essa regra.

### 2.3 Os quatro katas

| # | Pasta | Classe · método público | Retorno | Operação característica |
|---|---|---|---|---|
| K1 | `kata1-cinema` | `Bilheteria.totalCentavos(List<String>)` | escalar | tabela de preços + desconto não cumulativo |
| K2 | `kata2-torneio-luta` | `TorneioLuta.classificacao(List<String>)` | lista ordenada | ranking com três desempates |
| K3 | `kata3-calorias` | `ContadorCalorias.diasAcimaDaMeta(List<String>)` | lista ordenada | agregação com sinal + filtro por limiar |
| K4 | `kata4-gastos` | `OrganizadorGastos.saldoCentavos(List<String>)` | escalar | soma com sinal + divisão de parcela |

Os quatro se distribuem em **dois pares estruturais** — K1/K4 devolvem escalar, K2/K3 devolvem
lista ordenada — e, dentro de cada par, a operação característica é diferente. Isso mantém a
dificuldade equivalente sem que resolver um kata ensine a solução do seguinte (ameaça de
aprendizado entre katas, seção 4.5).

#### K1 · Bilheteria do Cinema

Registro: `id;sessao;categoria;dia` — `sessao` ∈ {`2d`,`3d`}, `categoria` ∈
{`inteira`,`estudante`,`idoso`,`crianca`}, `dia` ∈ {`seg`…`dom`}.

1. Preço cheio: 2D = 3200 centavos, 3D = 4400.
2. Estudante, idoso e criança pagam **meia** (50%, arredondado **para baixo**).
3. Terça e quarta são "dia do cinema": todo ingresso inteiro vira meia. **A meia não acumula** —
   quem já tinha direito continua pagando 50%, não 25%.
4. Sessão 3D cobra 500 de óculos por ingresso, somados **depois** e **fora** do cálculo da meia.

> Regra não-óbvia: a não-cumulatividade (3) e a ordem das operações em (4). Errar a ordem
> produz um resultado plausível mas errado — é o que os testes pegam.

#### K2 · Torneio de Jogos de Luta

Registro: `lutadorA;roundsA;roundsB;lutadorB`.

1. Vencedor da luta (mais rounds) ganha 3 pontos; perdedor, 0.
2. Rounds iguais = *double KO*: 1 ponto para cada.
3. Ordenação: pontos ↓ → saldo de rounds (vencidos − perdidos) ↓ → nº de vitórias
   **perfect** (2×0) ↓ → nome em ordem alfabética.
4. Saída: `"nome;pontos;saldo"` por lutador, na ordem da classificação.

> Regra não-óbvia: *perfect* como terceiro desempate. Não existe em nenhum kata de tabela de
> classificação conhecido.

#### K3 · Contador de Calorias

Registro: `dia;tipo;kcal;quantidade` — `dia` no formato `AAAA-MM-DD`, `tipo` ∈
{`refeicao`,`treino`}.

1. `refeicao` **soma** `kcal × quantidade` ao dia; `treino` **subtrai**.
2. Se o total do dia ficar negativo, ele **satura em 0** (não vira crédito para outro dia).
3. Meta diária: 2000 kcal. Só entram no resultado os dias **estritamente acima** da meta.
4. Dia que só tem registros de `treino` é **ignorado** — não aparece nem como 0.
5. Saída: `"dia;total"` em ordem cronológica crescente.

> Regras não-óbvias: a saturação (2) e o descarte do dia só-treino (4). Sem ler o enunciado, a
> saída natural seria manter o negativo e listar o dia.

#### K4 · Organizador de Gastos

Registro: `data;categoria;tipo;valor;parcelas` — `tipo` ∈ {`receita`,`despesa`}, `valor` em
centavos.

1. `receita` **soma** o valor integral ao saldo.
2. `despesa` **subtrai** `teto(valor / parcelas)` — só a parcela do mês entra no saldo.
3. `parcelas` é **ignorado** em receitas (receita parcelada não existe neste modelo).
4. Lançamento com `valor ≤ 0` ou `parcelas < 1` é **descartado**, sem erro.
5. O saldo pode ser negativo.

> Regras não-óbvias: arredondamento **para cima** na parcela (o oposto do K1, de propósito) e
> o descarte silencioso de registro inválido em (4).

#### Testes

Cada kata terá **10 testes de aceitação** — mesma quantidade nos quatro, para que a *taxa de
sucesso* da RQ2 seja diretamente comparável: 6 de caso normal, 2 de borda (lista vazia, valor
exatamente no limite) e 2 cobrindo a regra não-óbvia destacada acima.

### 2.4 Como a "dificuldade equivalente" será justificada

A justificativa não fica no "achamos que são parecidos" — ela é **medida**, e a ferramenta
usada é a do próprio experimento (`collect_metrics.py`, do Pedro), rodada sobre as
**soluções de referência**:

| Critério | Faixa-alvo | Como é verificado |
|---|---|---|
| LOC da solução de referência | 45 – 65 | `collect_metrics.py` (coluna `loc`) |
| WMC médio por método | 2,5 – 4,5 | `collect_metrics.py` (coluna `wmc_medio_por_metodo`) |
| Nº de métodos públicos | 1 (+ auxiliares privados) | inspeção / `qtd_metodos` |
| Nº de testes de aceitação | 10 (exatamente) | `check_katas.py` (coluna `tests_total`) |
| Forma algorítmica | parsing → regras → agregação | seção 2.2 |
| Tempo do piloto (solucionador de referência) | 15 – 20 min | trial piloto cronometrado com o `TrialTimer` |
| Dependências externas | só JUnit 5 | `pom.xml` idêntico entre os katas |

Um kata que sair da faixa é reescrito antes de fechar o card. O CSV com esses números vai para
`output/validacao-katas.csv` e é **a evidência do primeiro critério de aceitação do card**.

O **piloto** merece destaque: cada kata precisa ser resolvível em ~metade do time-box por
alguém que já conhece o enunciado. Se um kata consumir 30 dos 35 min no piloto, quase todos os
trials reais serão censurados e o dado da RQ1 perde resolução (todo mundo empata em 35:00).

---

## 3. Parte 2 — `check_katas.py` (artefato de código)

Script Python 3.10+, só biblioteca padrão, no mesmo estilo do `collect_metrics.py`
(argparse, funções em português, saída em CSV, mensagens em `stderr`).

### 3.1 O que ele faz, por kata

1. Copia o esqueleto `katas/<kata>/` para um diretório temporário.
2. **Sobrepõe** a solução de referência de `solucoes-referencia/<kata>/`.
3. Roda `mvn -B test` e faz *parsing* de `Tests run: X, Failures: Y, Errors: Z`
   — **o mesmo regex do `TrialTimer`**, de propósito: se o `check_katas` consegue ler o
   resultado, o `TrialTimer` também consegue. É isso que transforma esta etapa em um teste de
   integração de verdade, e não só em "os testes passam".
4. Roda `mvn -B test` de novo, agora sobre o **esqueleto puro** (sem a solução), e exige que
   **falhe**. Um kata cujos testes passam sem implementação é um kata com testes vazios — e
   isso invalidaria a RQ2 inteira.
5. Confere o SHA-256 de cada arquivo de teste contra `solucoes-referencia/MANIFEST.sha256`.

### 3.2 Interface

```bash
# a partir de desenho-experimento/
python scripts/check_katas.py --katas-dir katas --solucoes-dir solucoes-referencia \
                              --output output/validacao-katas.csv
```

| Argumento | Obrigatório | Default | Descrição |
|---|---|---|---|
| `--katas-dir` | não | `katas` | Diretório com um subdiretório por kata |
| `--solucoes-dir` | não | `solucoes-referencia` | Soluções de referência (sobrepostas ao esqueleto) |
| `--mvn-bin` | não | `mvn` | Executável do Maven (`mvn.cmd` no Windows) |
| `--output` | não | `output/validacao-katas.csv` | CSV consolidado |
| `--work-dir` | não | `output/raw` | Onde caem os logs do Maven quando uma verificação falha |
| `--skip-skeleton-check` | não | `false` | Pula a checagem "esqueleto deve falhar" (só para depuração) |
| `--offline` | não | `false` | Passa `-o` ao Maven (exige `~/.m2` já aquecido) |

**Colunas do CSV:** `kata, tests_total, tests_passing, tests_failures, referencia_green,
esqueleto_falha, hash_ok, duracao_seg, status`.

**Código de saída:** `0` se todos os katas ficam `status=OK`; `1` se qualquer um falha — o que
permite plugar o script num GitHub Action depois, se o grupo quiser.

### 3.3 Detalhes de ambiente já conhecidos

- **Resolvido (2026-09-10):** o ambiente tem JDK 21.0.7 (Zulu) e Python 3.13; o Maven foi
  instalado na versão **3.9.11** — a mesma que o Pedro documentou na RQ3. A Apache não publica
  Maven no `winget` e o `choco install maven` exige terminal elevado, então a instalação foi
  feita no perfil do usuário (`%LOCALAPPDATA%\Programs`), com checksum SHA-512 conferido. O
  procedimento está no [`README.md`](README.md#2-maven) da pasta, para Bruna e Pedro
  replicarem.
- No Windows o executável é `mvn.cmd` e só é resolvido via shell — mesmo problema que o Pedro
  já tratou em [`collect_metrics.py:29-31`](../rq3-metricas-estaticas/scripts/collect_metrics.py#L29-L31).
  A mesma solução (`shell=os.name == "nt"` + caminho absoluto) será reaproveitada.
- **Aquecer o `~/.m2` antes dos trials** (`mvn -B test` uma vez em cada kata). Se o download das
  dependências acontecer durante um trial, o tempo de rede entra no time-box e contamina a RQ1.

---

## 4. Parte 3 — Esboço do `DESIGN.md`

O `DESIGN.md` é escrito na tarefa T4; abaixo está o conteúdo já decidido, para o trio revisar
antes de eu redigir o documento final.

### 4.1 (A) Hipóteses

Uma por RQ, todas bicaudais na formulação nula:

| RQ | H0 | H1 |
|---|---|---|
| RQ1 | Não há diferença no *time-to-green* mediano entre trials com e sem assistente de IA | Há diferença (esperada: menor com IA) |
| RQ2 | Não há diferença na taxa de testes de aceitação passando ao fim do time-box | Há diferença |
| RQ3 | Não há diferença na complexidade ciclomática média (WMC) nem na % de linhas duplicadas, normalizadas por LOC | Há diferença |

### 4.2 (B)–(E) Variáveis, tratamentos e objetos

- **(B) Variáveis dependentes:** `time_to_green_sec` (censurado em 2100 s), `test_success_rate`,
  `wmc_medio_por_metodo`, `duplicacao_percentual`, `loc` (controle). Todas já são colunas dos
  CSVs que os scripts da Bruna e do Pedro produzem — nenhuma medição é feita à mão.
- **(C) Variável independente:** uso ou não do assistente de IA (fator de 2 níveis).
- **(D) Tratamentos:** `ia` (assistente habilitado, mesmo assistente e mesma versão para todos,
  registrada no relatório) e `manual` (assistente desabilitado; permitida documentação oficial
  da linguagem, proibida qualquer ferramenta generativa e busca por solução pronta).
- **(E) Objetos experimentais:** os 4 katas autorais da seção 2.3.

### 4.3 (F)–(G) Desenho e nº de medições

**Crossover within-subject contrabalanceado.** Cada integrante resolve os 4 katas, 2 com IA e
2 sem, em ordens diferentes entre integrantes (controla efeito de aprendizado) e com o
tratamento variando por kata entre integrantes (controla dificuldade residual de cada kata):

| Integrante | 1º | 2º | 3º | 4º |
|---|---|---|---|---|
| Bruna | K1 · **IA** | K2 · manual | K3 · **IA** | K4 · manual |
| Pedro | K3 · manual | K4 · **IA** | K1 · manual | K2 · **IA** |
| Thiago | K2 · **IA** | K1 · manual | K4 · manual | K3 · **IA** |

Balanceamento resultante: 6 trials com IA e 6 manuais; cada integrante 2 e 2; cada kata aparece
em posições diferentes da sequência; K1 e K4 saem 1×IA/2×manual e K2 e K3 saem 2×IA/1×manual
(o desbalanceamento residual por kata é inevitável com 3 integrantes — número ímpar — e será
declarado como limitação).

**(G) Nº de medições:** 12 trials × 5 variáveis dependentes = 60 medições, mais 4 trials-piloto
(não entram na análise; servem para calibrar a dificuldade dos katas).

### 4.4 Poder estatístico: por que 4 katas ainda permitem o Wilcoxon

O pareamento é o ponto delicado. Se pareássemos **por integrante** (mediana IA vs. mediana
manual de cada um), teríamos n = 3 pares — e o Wilcoxon pareado com n = 3 tem p mínimo de 0,25
bicaudal: seria impossível rejeitar H0 **qualquer que fosse o resultado**.

Então o pareamento será **por bloco intra-integrante**: cada integrante contribui com 2 pares
(1º trial IA × 1º trial manual, 2º × 2º), totalizando **n = 6 pares**. Com n = 6 o p mínimo do
Wilcoxon é 0,031 bicaudal (0,016 unicaudal) — abaixo de 0,05, então o teste é capaz de detectar
um efeito consistente. Isso vale para RQ1, RQ2 e RQ3, todas usando o mesmo pareamento.

Estatística descritiva: **mediana e IQR** (não média/desvio), conforme o roteiro.

### 4.5 (H) Ameaças à validade

| Ameaça | Tipo | Mitigação |
|---|---|---|
| **Memorização** — o assistente reproduz solução vista no treino | Interna | Katas autorais, inéditos, com domínios e regras inventadas |
| **Vazamento pelo repositório** — a solução de referência está no mesmo repo público que o participante abre na IDE, e o assistente lê arquivos abertos/vizinhos | Interna | Trials rodam em **cópia do esqueleto fora do repositório**; `solucoes-referencia/` nunca aberta durante trial; regra escrita no protocolo de execução |
| **Efeito de aprendizado** entre katas | Interna | Ordem contrabalanceada (tabela 4.3) |
| **Dificuldade residual desigual** entre katas | Interna | Faixas medidas da seção 2.4 + tratamento alternado por kata |
| **Variação individual de habilidade** | Interna | Desenho within-subject (cada um é seu próprio controle) |
| **Familiaridade prévia desigual com a ferramenta de IA** | Interna | Mesmo assistente e versão para todos; registrar experiência prévia autodeclarada |
| **Testes de aceitação enviesados** (escritos por nós) | Construto | Testes escritos **antes** das soluções; verificação "esqueleto deve falhar" no `check_katas`; hash dos arquivos de teste |
| **Censura em 35 min** achata a RQ1 | Conclusão | Censura registrada (`censored=true`), mediana em vez de média, piloto calibrando katas para ~20 min |
| **Overhead de medição** — o `TrialTimer` roda a suíte a cada 5 s | Conclusão | Custo idêntico nos dois tratamentos; resolução do `time_to_green` limitada ao intervalo de *polling* + tempo do Maven (~5–10 s) — declarar como erro sistemático |
| **N pequeno** (12 trials, 6 pares) | Conclusão | Teste não paramétrico; resultado não significativo será reportado como tal, sem extrapolação |
| **Generalização** — 3 estudantes, 4 katas, uma linguagem | Externa | Escopo declarado explicitamente no relatório |

### 4.6 Escolha e justificativa das métricas por RQ

| RQ | Métrica escolhida | Por quê (entre as candidatas do roteiro) |
|---|---|---|
| RQ1 | *time-to-green*, censurado em 2100 s, agregado por **mediana** | Métrica primária recomendada; mediana porque N é pequeno e a censura cria um teto artificial que arrastaria a média |
| RQ1 (exploratória) | nº de prompts | Já coletada de graça pelo `TrialTimer` (tecla `p`); usada só na discussão qualitativa |
| RQ2 | **taxa de sucesso** (% de testes passando) | Normaliza katas com números de teste diferentes — aqui todos têm 10, mas a taxa mantém a análise robusta se um kata precisar de ajuste |
| RQ2 (complementar) | nº absoluto de testes falhando | Mais legível no relatório |
| RQ3 | **WMC médio por método** e **% de linhas duplicadas** | As duas métricas pedidas; ambas já produzidas pelo `collect_metrics.py` |
| RQ3 (controle) | **LOC** | Obrigatória pelo roteiro: código gerado por IA tende a ser mais verboso, e complexidade/duplicação sem normalizar por LOC engana |

---

## 5. Protocolo de execução (insumo para a S02)

Sai deste card porque depende do formato dos katas, mas é executado na próxima sprint.
Vira uma seção do `README.md` da pasta:

1. Copiar `katas/<kata>/` para uma pasta de trabalho **fora do repositório**.
2. Abrir **só** essa pasta na IDE (nunca o repositório inteiro).
3. No tratamento `manual`, desabilitar o assistente na IDE **antes** de abrir a pasta.
4. Aquecer o Maven: `mvn -B test-compile` (fora do cronômetro).
5. Iniciar o trial:
   ```bash
   # a partir de rq1-rq2-tempo-e-testes/
   java -cp scripts TrialTimer --member thiago --kata kata2 --trial 1 --treatment ia \
        --test-cmd "mvn -B test" --cwd /caminho/da/copia/kata2-torneio-luta
   ```
6. Ao terminar, copiar o código final para `rq3-metricas-estaticas/trials/<membro>-<kata>-<tratamento>/`.
7. Fechar a Issue individual do trial no GitHub Projects, referenciando o commit.

---

## 6. Critérios de aceitação do card → onde cada um é cumprido

| Critério de aceitação (Issue #3) | Entregável | Tarefa |
|---|---|---|
| Lista final dos katas com justificativa de dificuldade equivalente | Seção "Katas" do `README.md` + `output/validacao-katas.csv` + tabela de métricas das referências | T1, T3, T6 |
| Script de validação versionado e executando (todos os katas *green* na referência) | `scripts/check_katas.py` + CSV commitado | T3, T5 |
| `DESIGN.md` completo revisado pelo trio | `DESIGN.md` + PR com review de Bruna e Pedro | T4, T7 |

---

## 7. Riscos e o que fazer

| Risco | Impacto | Resposta |
|---|---|---|
| ~~`mvn -q test` não expõe `Tests run:`~~ | ~~Alto~~ | ✅ **Confirmado e resolvido em T5** — usar `mvn -B test`. Falta aplicar a correção no README da RQ1/RQ2 (artefato da Bruna) |
| ~~Maven não instalado~~ | ~~Médio~~ | ✅ **Resolvido** — Maven 3.9.11 instalado e documentado no `README.md`; falta replicar nas máquinas da Bruna e do Pedro |
| **K2 é mais pesado que os demais** (72 LOC contra 41 do K4) | Médio | ⚠️ **Aberto** — desvio declarado no `README.md` e no `DESIGN.md`; o trial-piloto (T6) decide se o terceiro desempate do K2 sai |
| Download de dependências dentro do time-box | Médio | `~/.m2` aquecido + flag `--offline` no `check_katas` |
| Assistente de IA indexar o repositório e "ver" a solução de referência | **Alto** — invalida o tratamento `ia` | Trials em cópia fora do repo (protocolo, passos 1–2) |

---

## 8. Plano de commits e tarefas

Todo commit referencia `#3` — commits sem referência à Issue não são considerados na correção.

| # | Tarefa | Entrega | Commit sugerido |
|---|---|---|---|
| T1 | Escrever os 4 enunciados + `pom.xml` + esqueletos | `katas/*/ENUNCIADO.md`, `pom.xml`, esqueletos | `feat(katas): adiciona enunciados e esqueletos Maven dos 4 katas (#3)` |
| T2 | Escrever os 10 testes de aceitação de cada kata (**antes** das soluções) | `katas/*/src/test/...` | `test(katas): testes de aceitacao dos 4 katas (#3)` |
| T3 | Soluções de referência + `check_katas.py` | `solucoes-referencia/`, `scripts/check_katas.py` | `feat(validacao): script check_katas e solucoes de referencia (#3)` |
| T4 | Redigir o `DESIGN.md` (A–H + métricas por RQ) | `DESIGN.md` | `docs(design): desenho do experimento — hipoteses, variaveis e ameacas (#3)` |
| T5 | Rodar `check_katas` + smoke test de integração com o `TrialTimer` | `output/validacao-katas.csv` | `chore(validacao): resultado da validacao dos katas (#3)` |
| T6 | Trials-piloto e ajuste de dificuldade | tabela de métricas no `README.md` | `docs(katas): justificativa medida da dificuldade equivalente (#3)` |
| T7 | `README.md` da pasta + link no `README.md` da raiz + PR para review do trio | `README.md` (2 arquivos) | `docs: documenta pasta desenho-experimento (#3)` |

**Ordem obrigatória:** T2 antes de T3 (testes antes da solução — é o que sustenta a mitigação de
"testes enviesados" da seção 4.5). T5 e T6 só depois de T3. T7 fecha o card.

### Estado em 2026-09-10

| Tarefa | Estado |
|---|---|
| T1 · enunciados + esqueletos Maven | ✅ concluída |
| T2 · 40 testes de aceitação (10 por kata) | ✅ concluída |
| T3 · soluções de referência + `check_katas.py` | ✅ concluída |
| T4 · `DESIGN.md` | ✅ concluída |
| T5 · validação + smoke test de integração | ✅ concluída — 4/4 katas OK; bug do `-q` confirmado |
| T6 · métricas de dificuldade | 🟡 parcial — LOC/WMC/duplicação medidos; **falta o trial-piloto** |
| T7 · READMEs + PR para review | 🟡 READMEs prontos; falta abrir o PR |

**O que ainda impede fechar o card:** o trial-piloto de cada kata (T6) e os dois approves no
PR (T7). O piloto é o único item que pode gerar retrabalho — se o K2 estourar o tempo, o
terceiro critério de desempate sai e os testes daquele kata mudam.

---

## 9. O que precisa de decisão do trio

1. **Assistente de IA único para todos os trials** — Copilot (GitHub Student Pack) ou chatbot
   gratuito (ChatGPT/Claude/Gemini)? Precisa ser o mesmo para os três, e a versão vai no relatório.
2. **4 katas** (proposta deste plano) ou 6?
3. **Pareamento em n = 6** conforme a seção 4.4 — confirmar com quem for conduzir o Wilcoxon na S03.
4. **Regras dos katas** — as temáticas estão definidas (cinema, torneio de luta, calorias,
   gastos). Falta o trio revisar as *regras* de cada um na seção 2.3: alguma está ambígua, ou
   alguma regra não-óbvia ficou artificial demais a ponto de virar pegadinha em vez de
   especificação?
