# Errata da coleta — RQ1/RQ2

Registro de toda correção aplicada ao `results.csv` **depois** de o trial ter sido executado.

Princípio adotado: os **logs JSON em `output/logs/` nunca são editados** — são a saída bruta do
instrumento e servem de rastro de auditoria. Quando uma célula do `results.csv` é corrigida, a
divergência em relação ao JSON correspondente é esperada e fica documentada aqui, com o motivo.

Qualquer correção listada abaixo deve ser mencionada na Issue correspondente e discutida no
relatório final como limitação da coleta.

---

## C1 · 2026-09-17 — `prompts` do trial 1 do Thiago (kata2, `ia`): 0 → 1

| Campo | Valor |
|---|---|
| Trial | `member=thiago`, `kata=kata2`, `trial=1`, `treatment=ia` |
| Coluna | `prompts` |
| Valor gravado pelo instrumento | `0` |
| Valor corrigido no `results.csv` | `1` |
| Log JSON | `logs/kata2_t1_ia_20260917_193941.json` — **mantido com `0`** |

**O que aconteceu.** O participante enviou **uma** mensagem ao assistente (claude.ai, Sonnet 5),
colando o `ENUNCIADO.md` e o esqueleto; o código retornado passou nos 10 testes de primeira. Ele
esqueceu de pressionar `p` no `TrialTimer`, então a interação não foi contabilizada.

**Por que a correção é legítima.** O valor verdadeiro é conhecido sem ambiguidade — foi
exatamente uma mensagem, e o trial fechou em 55,9 s, compatível com um único ciclo de
colar → receber → colar de volta. A correção é de **anotação**, não de medição: nenhuma grandeza
cronometrada pelo instrumento foi alterada.

**Impacto na análise.** Nenhum sobre as hipóteses. `prompts` é métrica **exploratória**
([`DESIGN.md` §3](../../desenho-experimento/DESIGN.md#3-b-variáveis-dependentes)), usada apenas na
discussão qualitativa, sem teste estatístico. RQ1, RQ2 e RQ3 não dependem dela.

**Observação para o relatório.** O dado em si é interessante e merece menção na discussão: um
kata **autoral e inédito** foi resolvido corretamente com **um único prompt**, sem iteração. Isso
é evidência a favor de a mitigação da [ameaça #1](../../desenho-experimento/DESIGN.md#10-h-ameaças-à-validade)
(memorização) não ser o fator determinante — o assistente não podia ter visto este kata antes.
Vale contrastar com o número de prompts dos trials `ia` da Bruna e do Pedro.

**Prevenção.** O `check_trials_thiago.sh` falha quando um trial `ia` registra `prompts = 0`. Foi
esse check que detectou a omissão, no mesmo dia do trial.

---

## Ocorrências avaliadas e descartadas (sem correção)

| Data | Ocorrência | Conclusão |
|---|---|---|
| 2026-09-17 | `solucoes-referencia/kata1-cinema/Bilheteria.java` aberto no editor do participante no dia do trial 2 (kata1, `manual`) | **Sem impacto.** O arquivo foi aberto **após** o encerramento do trial, para conferência. O kata1 não é reexecutado por este participante, então não há contaminação de trial futuro. Registrado por transparência |
| 2026-09-17 | `target/` reapareceu dentro de `rq3-metricas-estaticas/trials/thiago-kata2-torneio-luta-ia/` depois da cópia | **Sem impacto.** Recriado pelo language server do Java (`redhat.java`) ao abrir o workspace; contém apenas `.class`, está coberto pelo `.gitignore` e não entra no commit nem na coleta da RQ3 |
