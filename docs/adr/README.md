# Architecture Decision Records (ADRs)

Um ADR registra uma decisão de arquitetura relevante do projeto: o contexto que a
motivou, as alternativas avaliadas, o que foi decidido e as consequências assumidas.
O objetivo é preservar o **porquê** das escolhas, que os guias operacionais deste
diretório não contam, e deixar rastro do que foi descartado no caminho.

A adoção dos ADRs neste projeto está registrada no
[ADR-0001](0001-registrar-decisoes-de-arquitetura.md).

## Índice

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [0001](0001-registrar-decisoes-de-arquitetura.md) | Registrar decisões de arquitetura | Aceito | 2026-09-05 |

## Formato

Os ADRs seguem o formato proposto por Michael Nygard, com as seções **Status**,
**Contexto**, **Decisão** e **Consequências**. O modelo a ser copiado está em
[`template.md`](template.md).

Nas **Consequências**, cada item é prefixado com `(+)` quando é positivo e `(-)` quando
é negativo, deixando explícitos os custos assumidos junto com os ganhos.

As alternativas descartadas são descritas dentro da seção **Contexto**. O formato Nygard
não tem uma seção própria para elas, e é importante que não se percam.

## Convenção de nomes

Cada ADR é um arquivo `NNNN-titulo-em-kebab-case.md`, com numeração sequencial de quatro
dígitos a partir de `0001`.

Números não são reaproveitados. Se um ADR for descontinuado ou substituído, o arquivo
permanece no diretório com o status atualizado e o número morre com ele.

## Ciclo de vida do status

```
Proposto  ->  Aceito  ->  Substituído por ADR-XXXX
                      ->  Descontinuado
```

- **Proposto**: decisão em discussão, ainda em revisão no Pull Request.
- **Aceito**: decisão vigente.
- **Substituído por ADR-XXXX**: outra decisão tomou o lugar desta.
- **Descontinuado**: a decisão deixou de valer e nada a substituiu.

Um ADR aceito é imutável. Se a decisão mudar, escreva um ADR novo e edite apenas a linha
de Status do antigo, com o link para o sucessor. Reescrever a decisão original apaga o
histórico, que é justamente o que o registro existe para guardar.

## Quando escrever um ADR

Escreva um ADR quando a decisão:

- for cara de reverter depois de implementada;
- afetar mais de um módulo ou mais de um dos repositórios do projeto;
- ou for previsivelmente questionada meses depois, por quem não participou da discussão.

A escolha de uma biblioteca utilitária pontual, restrita a um ponto do código e trocável
sem impacto, não precisa de ADR.

## Como criar um ADR

1. Copie [`template.md`](template.md) para `NNNN-titulo-em-kebab-case.md`, usando o
   próximo número livre do índice acima.
2. Preencha as seções, começando com o status `Proposto`.
3. Acrescente a linha correspondente na tabela do índice.
4. Abra um Pull Request, marque o impacto **Aplicação** no template de PR e referencie a
   issue relacionada.
5. Após a aprovação, altere o status para `Aceito`.