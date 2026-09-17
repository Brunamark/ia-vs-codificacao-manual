# Kata 1 — Bilheteria do Cinema

Implemente o método `totalCentavos` da classe `kata.Bilheteria`, que calcula quanto uma
bilheteria arrecadou a partir da lista de ingressos vendidos.

```java
public int totalCentavos(List<String> ingressos)
```

## Entrada

Uma lista de ingressos. Cada ingresso é uma `String` com quatro campos separados por `;`:

```
id;sessao;categoria;dia
```

| Campo | Valores possíveis |
|---|---|
| `id` | identificador do ingresso (qualquer texto, não usado no cálculo) |
| `sessao` | `2d` ou `3d` |
| `categoria` | `inteira`, `estudante`, `idoso` ou `crianca` |
| `dia` | `seg`, `ter`, `qua`, `qui`, `sex`, `sab` ou `dom` |

Exemplo: `A17;3d;estudante;qui`

Pode assumir que toda linha está bem formada e que todos os valores são válidos.

## Regras

1. **Preço cheio por sessão:** 2D custa **3295** centavos; 3D custa **4495** centavos.

2. **Meia-entrada por categoria:** ingressos de `estudante`, `idoso` e `crianca` pagam metade
   do preço cheio, **arredondada para baixo** (ex.: metade de 3295 é 1647, não 1648).

3. **Dia do cinema:** na **terça** (`ter`) e na **quarta** (`qua`), ingressos de categoria
   `inteira` também passam a pagar meia-entrada.
   ⚠️ **A meia-entrada não é cumulativa.** Quem já tinha direito a meia por categoria continua
   pagando 50% do preço cheio nesses dias — nunca 25%.

4. **Óculos 3D:** todo ingresso de sessão `3d` cobra **500** centavos de óculos.
   ⚠️ Esse valor é somado **depois** do desconto e **não** participa do cálculo da meia-entrada.
   Ele é cobrado **por ingresso**, não por venda.

## Saída

O valor total arrecadado, em centavos (`int`).

Uma lista vazia de ingressos resulta em `0`.

## Exemplos

| Ingresso | Cálculo | Valor |
|---|---|---|
| `1;2d;inteira;seg` | preço cheio 2D | `3295` |
| `2;2d;estudante;seg` | 3295 ÷ 2, arredondado para baixo | `1647` |
| `3;3d;inteira;seg` | 4495 + 500 de óculos | `4995` |
| `4;3d;idoso;seg` | (4495 ÷ 2 → 2247) + 500 | `2747` |
| `5;2d;inteira;ter` | dia do cinema: vira meia | `1647` |
| `6;2d;estudante;ter` | já era meia; **não acumula** | `1647` |

Para a lista `["1;2d;inteira;seg", "2;3d;estudante;qui", "3;2d;crianca;ter", "4;3d;inteira;qua"]`
o resultado é `3295 + 2747 + 1647 + 2747 = 10436`.
