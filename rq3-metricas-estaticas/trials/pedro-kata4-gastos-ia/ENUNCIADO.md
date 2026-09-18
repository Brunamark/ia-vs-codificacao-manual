# Kata 4 — Organizador de Gastos

Implemente o método `saldoCentavos` da classe `kata.OrganizadorGastos`, que calcula o saldo do
mês a partir dos lançamentos financeiros.

```java
public int saldoCentavos(List<String> lancamentos)
```

## Entrada

Uma lista de lançamentos. Cada lançamento é uma `String` com cinco campos separados por `;`:

```
data;categoria;tipo;valor;parcelas
```

| Campo | Descrição |
|---|---|
| `data` | data no formato `AAAA-MM-DD` (não usada no cálculo) |
| `categoria` | texto livre (ex.: `mercado`, `aluguel`) — não usada no cálculo |
| `tipo` | `receita` ou `despesa` |
| `valor` | valor **total** do lançamento, em centavos |
| `parcelas` | em quantas parcelas a despesa foi dividida |

Exemplo: `2026-09-05;notebook;despesa;700000;10`

Pode assumir que toda linha está bem formada e que `valor` e `parcelas` são inteiros.

## Regras

1. **Receita:** soma ao saldo o **valor integral**.

2. **Despesa:** subtrai do saldo apenas a **parcela deste mês**, igual a `valor ÷ parcelas`
   **arredondado para cima**.
   ⚠️ Arredondamento para **cima**: uma despesa de 10000 em 3 parcelas desconta **3334**, não
   3333.

3. **Parcelas em receita:** o campo `parcelas` é **ignorado** quando o tipo é `receita`.
   ⚠️ Uma receita de 90000 com `parcelas` igual a 3 soma **90000** ao saldo, não 30000.

4. **Lançamento inválido:** descarte, sem gerar erro, todo lançamento com `valor` menor ou
   igual a `0`, ou com `parcelas` menor que `1` — inclusive quando for receita.

5. **O saldo pode ser negativo.**

## Saída

O saldo do mês em centavos (`int`), positivo ou negativo.

Uma lista vazia de lançamentos resulta em `0`.

## Exemplos

| Lançamento | Efeito no saldo |
|---|---|
| `2026-09-01;salario;receita;500000;1` | `+500000` |
| `2026-09-01;mercado;despesa;15000;1` | `-15000` |
| `2026-09-05;eletro;despesa;120000;12` | `-10000` |
| `2026-09-05;curso;despesa;10000;3` | `-3334` (arredondado para cima) |
| `2026-09-01;bonus;receita;90000;3` | `+90000` (`parcelas` ignorado) |
| `2026-09-01;erro;despesa;9000;0` | `0` (descartado) |

Para a lista

```java
["2026-09-01;salario;receita;300000;1",
 "2026-09-02;aluguel;despesa;180000;1",
 "2026-09-03;notebook;despesa;700000;10",
 "2026-09-04;curso;despesa;10000;3",
 "2026-09-05;freela;receita;50000;2",
 "2026-09-06;mercado;despesa;125000;1"]
```

o saldo é `300000 − 180000 − 70000 − 3334 + 50000 − 125000 = -28334`.
