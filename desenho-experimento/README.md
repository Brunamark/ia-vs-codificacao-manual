# Desenho do experimento e katas

Objeto experimental e desenho do **Laboratório 02 — Assistentes de IA vs. codificação
manual**. Enquanto as pastas `rq1-rq2-tempo-e-testes/` e `rq3-metricas-estaticas/` contêm as
ferramentas que **medem**, esta contém **o que é medido** (os katas) e o **documento que
define o experimento**.

| Arquivo | Conteúdo |
|---|---|
| [`DESIGN.md`](DESIGN.md) | Desenho do experimento — hipóteses, variáveis, tratamentos, contrabalanceamento, análise estatística e ameaças à validade (Passo 1 do roteiro) |
| [`PLANO-ISSUE-3.md`](PLANO-ISSUE-3.md) | Plano de execução da Issue #3: escopo, contratos com os outros artefatos, riscos e plano de commits |
| `katas/` | Os 4 katas entregues aos participantes (enunciado + projeto Maven + testes) |
| `solucoes-referencia/` | Soluções de referência — **não** entregues; usadas só para validação |
| `scripts/check_katas.py` | Validação automatizada dos katas |
| `output/validacao-katas.csv` | Evidência da validação (versionado) |
| `output/metricas-referencia.csv` | Métricas estáticas das soluções de referência, base da justificativa de dificuldade equivalente (versionado) |

## Os katas

Quatro katas **autorais** em Java, inéditos até a publicação deste repositório. Todos seguem a
mesma forma algorítmica — recebem `List<String>` de registros separados por `;`, fazem
*parsing*, aplicam uma tabela de regras de negócio e agregam — mas se dividem em dois pares
estruturais para que resolver um não ensine a solução do seguinte.

| # | Kata | Classe · método | Retorno | Regra deliberadamente não-óbvia |
|---|---|---|---|---|
| K1 | [Bilheteria do Cinema](katas/kata1-cinema/ENUNCIADO.md) | `Bilheteria.totalCentavos` | escalar | a meia-entrada **não acumula** com o dia do cinema; os óculos 3D ficam **fora** do desconto |
| K2 | [Torneio de Jogos de Luta](katas/kata2-torneio-luta/ENUNCIADO.md) | `TorneioLuta.classificacao` | lista ordenada | *perfect* é definido pelo adversário terminar com **0 rounds**, não pelo placar ser 2×0 |
| K3 | [Contador de Calorias](katas/kata3-calorias/ENUNCIADO.md) | `ContadorCalorias.diasAcimaDaMeta` | lista ordenada | treino **subtrai** calorias; linhas idênticas contam **uma vez** |
| K4 | [Organizador de Gastos](katas/kata4-gastos/ENUNCIADO.md) | `OrganizadorGastos.saldoCentavos` | escalar | parcela arredonda **para cima**; `parcelas` é **ignorado** em receitas |

Cada kata tem exatamente **10 testes de aceitação**, para que a taxa de sucesso da RQ2 seja
diretamente comparável entre eles.

### Por que autorais

Um exercício clássico (FizzBuzz, números romanos, boliche) pode ser reproduzido de memória
pelo assistente de IA a partir do treinamento — o experimento mediria recuperação de solução
vista, não auxílio à resolução. Dois dos domínios (calorias e gastos) são temas comuns de
tutorial; por isso cada kata carrega ao menos uma regra contraintuitiva, que só se acerta
lendo o `ENUNCIADO.md`. Ver a seção de ameaças à validade do [`DESIGN.md`](DESIGN.md).

### Justificativa da dificuldade equivalente

Medida, não declarada. Os números abaixo saem do `collect_metrics.py` (RQ3) rodado sobre as
**soluções de referência**, e do `check_katas.py` — estão versionados em
`output/metricas-referencia.csv` e `output/validacao-katas.csv`.

Medições de **2026-09-10**, com CK `0.7.1-SNAPSHOT` (commit `8a1ef91`), PMD 7.27.0
(`--min-tokens 50`), Maven 3.9.11 e JDK 21.0.7:

| Kata | LOC | Métodos | WMC médio | Duplicação | Testes | Validação |
|---|---:|---:|---:|---:|---:|---|
| kata1-cinema | 48 | 3 | 2,33 | 0,0% | 10/10 | OK |
| kata2-torneio-luta | 72 | 3 | 2,67 | 0,0% | 10/10 | OK |
| kata3-calorias | 53 | 2 | 4,00 | 0,0% | 10/10 | OK |
| kata4-gastos | 41 | 3 | 2,33 | 0,0% | 10/10 | OK |
| **Faixa-alvo pré-registrada** | 45–65 | — | 2,5–4,5 | — | 10 | — |

**O que confere:** todos os katas têm exatamente 10 testes, um único método público (mais
auxiliares privados), nenhuma duplicação interna, e todos ficam *green* na referência com o
esqueleto falhando.

**O que não confere — desvio declarado.** Duas medições saíram fora da faixa que registramos
*antes* de implementar:

- **LOC:** `kata2-torneio-luta` (72) ficou acima e `kata4-gastos` (41) abaixo da faixa 45–65 —
  uma dispersão de 1,76×. Não é estilo de escrita: o K2 é o único que exige modelar uma
  entidade com três acumuladores e ordenar por quatro critérios, enquanto o K4 é uma soma
  sem estado. O K2 é, de fato, o kata mais pesado dos quatro.
- **WMC:** `kata1` e `kata4` (2,33) ficaram levemente abaixo do piso de 2,5, puxados por
  métodos auxiliares curtos.

A faixa **não foi alargada para acomodar o resultado** — isso seria racionalizar depois do
fato. O desvio fica registrado como limitação, com três mitigações:

1. O contrabalanceamento distribui **cada kata** entre os dois tratamentos, então um kata mais
   pesado não favorece sistematicamente `ia` nem `manual`.
2. Os quatro têm a mesma forma algorítmica, o mesmo número de regras de negócio (4) e o mesmo
   número de testes (10).
3. O critério decisivo é o **tempo do trial-piloto** (15–20 min), ainda pendente. Se o piloto
   confirmar que o K2 leva materialmente mais tempo, a correção é remover o terceiro critério
   de desempate (*perfect*) — decisão do trio.

## Setup

### 1. JDK 17 ou superior

Exigido pelos katas (`maven.compiler.release` 17) e pelo CK da RQ3.
Ambiente de referência: **JDK 21.0.7 (Zulu)**.

### 2. Maven

Apache Maven **3.9.11** (mesma versão usada na RQ3). O Maven é obrigatório — não é uma
preferência de build: o **Surefire** é quem imprime `Tests run: X, Failures: Y, Errors: Z`,
padrão que o `TrialTimer` da RQ1/RQ2 lê para detectar o *green*.

A Apache não publica Maven no `winget`, e o `choco install maven` exige terminal elevado. Sem
admin, instale no perfil do usuário:

```powershell
$ver = "3.9.11"
$zip = "$env:TEMP\apache-maven-$ver-bin.zip"
Invoke-WebRequest -UseBasicParsing -OutFile $zip `
  "https://archive.apache.org/dist/maven/maven-3/$ver/binaries/apache-maven-$ver-bin.zip"
Expand-Archive $zip "$env:LOCALAPPDATA\Programs" -Force
$bin = "$env:LOCALAPPDATA\Programs\apache-maven-$ver\bin"
[Environment]::SetEnvironmentVariable('Path',
  [Environment]::GetEnvironmentVariable('Path','User') + ";$bin", 'User')
```

> Reabra o terminal para o PATH valer. Enquanto isso, passe `--mvn-bin` com o caminho completo.

### 3. Python 3.10+

Só a biblioteca padrão (`csv`, `hashlib`, `shutil`, `subprocess`, `tempfile`) — nenhum
`pip install`.

### 4. Aquecer o cache do Maven

**Antes de qualquer trial**, rode uma vez em cada kata:

```bash
mvn -B test-compile
```

Se o download das dependências acontecer durante um trial, o tempo de rede entra no time-box e
contamina a RQ1.

## Validando os katas

```bash
# a partir de desenho-experimento/
python scripts/check_katas.py
```

No Windows, se o Maven ainda não estiver no PATH da sessão:

```powershell
python scripts/check_katas.py --mvn-bin "$env:LOCALAPPDATA\Programs\apache-maven-3.9.11\bin\mvn.cmd"
```

O script faz três verificações por kata:

1. **Green na referência** — sobrepõe a solução de referência ao esqueleto e exige que os 10
   testes passem.
2. **Esqueleto falha** — roda os testes sobre o esqueleto puro e exige que **falhem**. Um kata
   cujos testes passam sem implementação tem testes vazios e invalidaria a RQ2.
3. **Integridade dos testes** — confere o SHA-256 de cada arquivo de teste contra
   `solucoes-referencia/MANIFEST.sha256`, garantindo que ninguém os alterou.

O *parsing* da saída do Maven usa **o mesmo padrão do `TrialTimer`** de propósito: se este
script lê o resultado, o `TrialTimer` também lê. Isso torna a validação um teste de integração
real entre os dois artefatos.

### Argumentos

| Argumento | Obrigatório | Default | Descrição |
|---|---|---|---|
| `--katas-dir` | não | `katas` | Diretório com um subdiretório por kata |
| `--solucoes-dir` | não | `solucoes-referencia` | Soluções de referência sobrepostas ao esqueleto |
| `--mvn-bin` | não | `mvn` | Executável do Maven (`mvn.cmd` no Windows) |
| `--output` | não | `output/validacao-katas.csv` | CSV consolidado |
| `--work-dir` | não | `output/raw` | Onde caem os logs do Maven quando uma verificação falha |
| `--offline` | não | `false` | Passa `-o` ao Maven (exige `~/.m2` aquecido) |
| `--skip-skeleton-check` | não | `false` | Pula a checagem "esqueleto deve falhar" (depuração) |

Código de saída: `0` se todos os katas ficam `OK`; `1` caso contrário — dá para plugar em CI.

### Colunas do CSV

`kata, tests_total, tests_passing, tests_failures, referencia_green, esqueleto_falha,
hash_ok, duracao_seg, status`

### Sobre o MANIFEST.sha256

Os hashes são calculados com as quebras de linha **normalizadas para LF**. O Git converte LF em
CRLF ao fazer checkout no Windows, então o hash do arquivo bruto mudaria conforme o sistema
operacional do integrante — normalizar mantém o manifesto válido nas três máquinas do grupo.

Para regerar o manifesto depois de uma mudança legítima nos testes:

```bash
python - <<'PY'
import hashlib, pathlib
raiz = pathlib.Path('.')
linhas = ['# SHA-256 dos arquivos de teste (quebras de linha normalizadas para LF).']
for t in sorted(raiz.glob('katas/*/src/test/java/kata/*.java')):
    h = hashlib.sha256(t.read_bytes().replace(b'\r\n', b'\n')).hexdigest()
    linhas.append(f'{h}  {t.as_posix()}')
pathlib.Path('solucoes-referencia/MANIFEST.sha256').write_text('\n'.join(linhas) + '\n', encoding='utf-8')
PY
```

## Protocolo de execução dos trials (S02)

⚠️ Use `mvn -B test` como `--test-cmd`, **nunca `mvn -q test`** — ver a nota de integração
abaixo.

1. Copiar `katas/<kata>/` para uma pasta de trabalho **fora deste repositório**.
2. No tratamento `manual`, desabilitar o assistente na IDE **antes** de abrir a pasta.
3. Abrir **somente** a pasta copiada na IDE — nunca o repositório inteiro, para que o
   assistente não leia a solução de referência como contexto.
4. Aquecer o Maven (`mvn -B test-compile`) fora do cronômetro.
5. Ler o `ENUNCIADO.md` e iniciar o cronômetro:
   ```bash
   # a partir de rq1-rq2-tempo-e-testes/
   java -cp scripts TrialTimer --member <nome> --kata <kataN> --trial <n> \
        --treatment <ia|manual> --test-cmd "mvn -B test" --cwd <pasta-de-trabalho>
   ```
6. No tratamento `ia`, pressionar `p` a cada interação com o assistente.
7. Ao encerrar, copiar o código final para
   `rq3-metricas-estaticas/trials/<membro>-<kata>-<tratamento>/`.
8. Fechar a Issue individual do trial no GitHub Projects, referenciando o commit.

A ordem dos katas e o tratamento de cada integrante estão fixados na tabela de
contrabalanceamento do [`DESIGN.md`](DESIGN.md#7-f-tipo-de-projeto-experimental).

## Nota de integração: `mvn -q test` quebra a medição da RQ1

**Verificado empiricamente** com Maven 3.9.11 e Surefire 3.2.5:

| Comando | Resultado | `Tests run:` na saída? |
|---|---|---|
| `mvn -q test` | sucesso | **não** — a saída é completamente vazia |
| `mvn -q test` | falha | **não** |
| `mvn -B test` | sucesso | sim (`Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`) |
| `mvn -B test` | falha | sim (`Tests run: 10, Failures: 5, Errors: 0, Skipped: 0`) |

O `-q` suprime as mensagens de nível INFO, e o resumo do Surefire é INFO. Como o `TrialTimer`
exige `tests_total > 0` para declarar *green*, um trial rodado com `mvn -q test` **nunca
ficaria green** e seria censurado em 35 min — e as contagens da RQ2 sairiam todas zeradas.

Isso afeta o exemplo documentado em
[`rq1-rq2-tempo-e-testes/README.md`](../rq1-rq2-tempo-e-testes/README.md). A correção é trocar
`-q` por `-B` nos exemplos; **o código do `TrialTimer` está correto e não precisa mudar**.
