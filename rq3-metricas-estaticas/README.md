# Coleta de métricas estáticas — RQ3

Ambiente e script usados para coletar métricas estáticas do código final de
cada trial (kata) do experimento, para responder a **RQ3**:

- **Complexidade ciclomática média por método (WMC)** — via [CK](https://github.com/mauricioaniche/ck)
- **% de linhas duplicadas** — via PMD CPD
- **LOC** — métrica de controle

O resultado é um único CSV consolidado, com **uma linha por trial**, pronto
para a análise da S03.

> Todos os caminhos abaixo são relativos a esta pasta (`rq3-metricas-estaticas/`).
> Rode os comandos a partir daqui: `cd rq3-metricas-estaticas`.

## Por que só katas em Java

CK e PMD/CPD (na configuração usada aqui) são ferramentas específicas para
Java: o CK analisa bytecode/AST Java via Eclipse JDT, e o CPD é invocado com
`--language java`. Por isso, **todos os katas usados nesta coleta devem ser
implementados em Java** — isso é um requisito da ferramenta, não uma escolha
arbitrária. Um trial em outra linguagem produziria métricas zeradas ou um
erro do CK.

## Versões usadas (ambiente de referência)

| Ferramenta | Versão | Observação |
|---|---|---|
| Java (JDK) | 25.0.4.1 LTS (Oracle) | Mínimo exigido pelo CK: Java 17+ |
| Maven | 3.9.11 | Usado só para compilar o CK a partir do código-fonte |
| CK | `0.7.1-SNAPSHOT`, commit [`8a1ef91`](https://github.com/mauricioaniche/ck/commit/8a1ef916d597d33ad619d748b616bb7dbc3f0c36) (2026-04-29) | Projeto não publica releases/jar prontos — é preciso compilar a partir do source (veja abaixo) |
| PMD (CPD) | 7.27.0 | [Release oficial](https://github.com/pmd/pmd/releases/tag/pmd_releases%2F7.27.0) — distribuição binária, não precisa compilar |
| Python | 3.10+ | Usado só para orquestrar o script de coleta (`collect_metrics.py`), sem dependências externas (biblioteca padrão) |

## Estrutura de diretórios

```
rq3-metricas-estaticas/
├── README.md                # este arquivo
├── trials/                  # um subdiretório por trial, com o código final do kata (versionado)
│   └── exemplo-fizzbuzz/    # trial de exemplo usado para gerar o CSV de exemplo
├── tools/                   # CK (compilado) e PMD (extraído) — NÃO versionado, ver setup abaixo
├── scripts/
│   └── collect_metrics.py   # script de coleta (versionado)
└── output/
    ├── exemplo-metrics.csv  # CSV de exemplo, gerado a partir de trials/exemplo-fizzbuzz (versionado)
    ├── metrics.csv          # CSV gerado ao rodar sobre os trials reais (não versionado; cada execução regenera)
    └── raw/                 # saída bruta do CK (class/method/variable.csv) e do CPD (cpd.xml) por trial — NÃO versionado
```

`tools/` e `output/raw/` estão no `.gitignore` do repositório: são artefatos
binários grandes ou totalmente reprodutíveis a partir do script, então não
fazem sentido versionados. O que importa para a análise (o CSV consolidado)
é versionado.

## Setup do ambiente

### 1. Java e Maven

Precisa de **JDK 17 ou superior** e Maven no PATH (`java -version`, `mvn -version`).

### 2. CK (compilar a partir do source)

O repositório do CK não publica um `.jar` pronto, então é preciso compilar:

```bash
git clone https://github.com/mauricioaniche/ck.git tools/ck-src
cd tools/ck-src
mvn -q -DskipTests clean package
cd ../..
```

Isso gera `tools/ck-src/target/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar`.

### 3. PMD (baixar a distribuição binária)

```bash
curl -sL -o tools/pmd-dist-7.27.0-bin.zip \
  "https://github.com/pmd/pmd/releases/download/pmd_releases%2F7.27.0/pmd-dist-7.27.0-bin.zip"
cd tools && unzip -q pmd-dist-7.27.0-bin.zip && cd ..
```

Isso extrai `tools/pmd-bin-7.27.0/bin/pmd` (Linux/macOS) ou
`pmd.bat` (Windows).

### 4. Python

Só a biblioteca padrão é usada (`csv`, `xml.etree.ElementTree`, `subprocess`,
`argparse`, `statistics`) — nenhum `pip install` é necessário.

## Adicionando um trial

Crie um subdiretório em `trials/<nome-do-trial>/` e coloque ali o código Java
final daquele trial (a estrutura interna, com ou sem Maven, não importa — o
CK e o CPD escaneiam `.java` recursivamente).

## Rodando o script

```bash
python scripts/collect_metrics.py \
  --trials-dir trials \
  --ck-jar tools/ck-src/target/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar \
  --pmd-bin tools/pmd-bin-7.27.0/bin/pmd.bat \
  --min-tokens 50 \
  --output output/metrics.csv
```

No Windows, use `pmd.bat`; no Linux/macOS, use `bin/pmd` (sem extensão).

`--min-tokens` é o `--minimum-tokens` repassado ao PMD CPD: o tamanho mínimo
(em tokens) de um trecho para ser considerado duplicado. O CSV de exemplo
deste repositório foi gerado com `--min-tokens 30` (adequado para os métodos
curtos do kata de exemplo); para trials maiores, o valor padrão do PMD (50)
costuma ser mais adequado.

### Argumentos

| Argumento | Obrigatório | Default | Descrição |
|---|---|---|---|
| `--trials-dir` | não | `trials` | Diretório com um subdiretório por trial |
| `--ck-jar` | sim | — | Caminho do jar-with-dependencies do CK |
| `--pmd-bin` | sim | — | Caminho do executável `pmd`/`pmd.bat` |
| `--java-bin` | não | `java` | Executável Java usado para rodar o CK |
| `--min-tokens` | não | `50` | `--minimum-tokens` do PMD CPD |
| `--output` | não | `output/metrics.csv` | CSV consolidado de saída |
| `--work-dir` | não | `output/raw` | Onde ficam os relatórios brutos de CK/CPD por trial |

## Metodologia e colunas do CSV

Cada linha do CSV de saída corresponde a um trial:

| Coluna | Origem | Definição |
|---|---|---|
| `trial` | nome do subdiretório | Identificador do trial |
| `loc` | contagem própria do script | Total de linhas físicas (incluindo comentários e linhas em branco) somado em todos os `.java` do trial. Serve como métrica de controle — não depende de CK/PMD, então é estável entre versões dessas ferramentas |
| `qtd_metodos` | CK (`method.csv`) | Número de métodos analisados no trial |
| `wmc_medio_por_metodo` | CK (`method.csv`, coluna `wmc`) | Média aritmética do WMC (McCabe/complexidade ciclomática) de todos os métodos do trial |
| `linhas_duplicadas` | PMD CPD (XML) | Soma de `lines × ocorrências` de cada bloco duplicado reportado pelo CPD |
| `duplicacao_percentual` | calculado | `linhas_duplicadas / loc × 100` |

**Observações de metodologia:**

- O CPD detecta clones **por token exato** (Type-1/Type-2): variáveis com
  nomes diferentes em blocos por outro lado idênticos não são detectadas como
  duplicação, a menos que se rode com `--ignore-identifiers` (não usado por
  padrão aqui, para manter o critério mais conservador e comparável entre
  trials).
- `linhas_duplicadas` conta **todas** as ocorrências de cada bloco (inclusive
  a primeira), então blocos duplicados sobrepostos podem, em tese, levar o
  percentual acima de 100%. Isso é uma aproximação aceitável para a RQ3, cujo
  objetivo é comparação relativa entre trials — a mesma fórmula é aplicada a
  todos.
- `wmc_medio_por_metodo` é uma média simples (não ponderada por LOC do
  método); construtores contam como métodos separados, conforme o próprio CK.

## CSV de exemplo

`output/exemplo-metrics.csv` foi gerado rodando o comando acima (com
`--min-tokens 30`) sobre `trials/exemplo-fizzbuzz/`, um kata de FizzBuzz com
duplicação proposital entre `FizzBuzz.numeroValido` e
`FizzBuzzValidator.numeroValido`, para exercitar as três métricas:

```csv
trial,loc,qtd_metodos,wmc_medio_por_metodo,linhas_duplicadas,duplicacao_percentual
exemplo-fizzbuzz,80,5,4.6,28,35.0
```
