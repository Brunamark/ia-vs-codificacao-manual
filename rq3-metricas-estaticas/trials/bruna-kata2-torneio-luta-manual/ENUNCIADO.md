# Kata 2 — Torneio de Jogos de Luta

Implemente o método `classificacao` da classe `kata.TorneioLuta`, que monta a tabela de
classificação de um torneio a partir dos resultados das lutas.

```java
public List<String> classificacao(List<String> lutas)
```

## Entrada

Uma lista de lutas. Cada luta é uma `String` com quatro campos separados por `;`:

```
lutadorA;roundsA;roundsB;lutadorB
```

`roundsA` e `roundsB` são inteiros ≥ 0 com os rounds vencidos por cada lutador.

Exemplo: `Ryu;2;0;Ken` — Ryu venceu Ken por 2 rounds a 0.

Pode assumir que toda linha está bem formada. Um mesmo lutador pode aparecer em várias lutas.

## Regras

1. **Pontuação:** quem venceu mais rounds na luta ganha **3 pontos**; o perdedor ganha **0**.

2. **Double KO:** se os dois lutadores vencerem o **mesmo número de rounds** (inclusive `0;0`),
   a luta é um *double KO* e **cada um ganha 1 ponto**.

3. **Saldo de rounds:** para cada lutador, saldo = rounds vencidos − rounds perdidos, somado
   em todas as lutas de que participou.

4. **Vitória perfect:** uma vitória em que o **adversário não venceu nenhum round** conta como
   *perfect*.
   ⚠️ O que define *perfect* é o adversário terminar com **0 rounds** — não o placar ser
   exatamente `2;0`. Uma vitória por `3;0` também é *perfect*; uma por `3;1` não é.
   Um *double KO* `0;0` **não** é *perfect* para ninguém, porque ninguém venceu.

5. **Ordem da classificação**, aplicando os critérios em sequência:
   1. maior número de pontos;
   2. maior saldo de rounds;
   3. maior número de vitórias *perfect*;
   4. nome em ordem alfabética crescente.

## Saída

Uma `List<String>` com um elemento por lutador que apareceu em ao menos uma luta, na ordem da
classificação, no formato:

```
nome;pontos;saldo
```

Uma lista vazia de lutas resulta em uma lista vazia.

## Exemplos

Para `["Ryu;2;0;Ken"]`:

```
["Ryu;3;2", "Ken;0;-2"]
```

Para `["Ryu;1;1;Ken"]` (double KO — empate em tudo, desempata pelo nome):

```
["Ken;1;0", "Ryu;1;0"]
```

Para `["Ryu;2;0;Ken", "Akuma;3;1;Guile"]` — Ryu e Akuma têm 3 pontos e saldo +2, mas só Ryu
venceu por *perfect*:

```
["Ryu;3;2", "Akuma;3;2", "Guile;0;-2", "Ken;0;-2"]
```
