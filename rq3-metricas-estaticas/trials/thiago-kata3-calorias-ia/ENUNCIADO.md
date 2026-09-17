# Kata 3 — Contador de Calorias

Implemente o método `diasAcimaDaMeta` da classe `kata.ContadorCalorias`, que identifica em
quais dias a pessoa passou da meta calórica.

```java
public List<String> diasAcimaDaMeta(List<String> registros)
```

## Entrada

Uma lista de registros. Cada registro é uma `String` com quatro campos separados por `;`:

```
dia;tipo;valor;quantidade
```

| Campo | Descrição |
|---|---|
| `dia` | data no formato `AAAA-MM-DD` (ex.: `2026-09-10`) |
| `tipo` | `refeicao` ou `treino` |
| `valor` | calorias de **uma** unidade: ingeridas (`refeicao`) ou gastas (`treino`) |
| `quantidade` | quantas unidades (porções ou sessões) |

Exemplo: `2026-09-10;refeicao;800;3` — três porções de 800 kcal.

Pode assumir que toda linha está bem formada e que `valor` e `quantidade` são inteiros.

## Regras

1. **Total do dia:** some `valor × quantidade` de cada registro do tipo `refeicao` e
   **subtraia** `valor × quantidade` de cada registro do tipo `treino`.
   ⚠️ Treino **desconta** do total do dia — não é apenas informativo.

2. **Envio duplicado:** duas linhas **exatamente iguais** representam o mesmo registro enviado
   duas vezes e devem contar **uma única vez**.
   ⚠️ A comparação é da linha inteira: `2026-09-10;refeicao;1200;2` repetido conta uma vez, mas
   `2026-09-10;refeicao;1200;1` e `2026-09-10;refeicao;1200;1` em dias diferentes são registros
   distintos e ambos contam.

3. **Registro inválido:** descarte, sem gerar erro, todo registro com `quantidade` menor ou
   igual a `0`, ou com `valor` negativo.

4. **Meta diária:** 2000 kcal. Entram no resultado apenas os dias **estritamente acima** da
   meta — um dia com exatamente 2000 kcal **não** entra.

## Saída

Uma `List<String>` com um elemento por dia acima da meta, em **ordem cronológica crescente**,
no formato:

```
dia;total
```

Uma lista vazia de registros resulta em uma lista vazia.

## Exemplos

| Registros | Total | Resultado |
|---|---|---|
| `["2026-09-10;refeicao;2500;1"]` | 2500 | `["2026-09-10;2500"]` |
| `["2026-09-10;refeicao;2000;1"]` | 2000 | `[]` (não é *estritamente* acima) |
| `["2026-09-10;refeicao;800;3"]` | 2400 | `["2026-09-10;2400"]` |
| `["2026-09-10;refeicao;2500;1", "2026-09-10;treino;600;1"]` | 1900 | `[]` |
| `["2026-09-10;refeicao;1200;2", "2026-09-10;refeicao;1200;2"]` | 2400 | `["2026-09-10;2400"]` |

Para a lista

```java
["2026-09-12;refeicao;2600;1",
 "2026-09-10;refeicao;2100;1",
 "2026-09-11;refeicao;1000;1",
 "2026-09-11;refeicao;1500;1"]
```

o resultado é `["2026-09-10;2100", "2026-09-11;2500", "2026-09-12;2600"]`.
