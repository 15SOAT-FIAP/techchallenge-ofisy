# RFC-NNNN. Título curto da proposta

Data: AAAA-MM-DD
Autor: @usuario-github de quem propõe

## Status

Proposta | Em discussão | Aceita | Rejeitada | Adiada | Substituída por [RFC-XXXX](XXXX-titulo.md)

## Resumo

Um parágrafo explicando a mudança proposta. Quem ler só esta seção precisa entender o
que está sendo pedido e por quê.

## Motivação

Qual problema existe hoje, descrito em fatos e no presente. Cite o comportamento atual
do sistema e, quando houver, o ADR que registrou a decisão que se pretende revisar.

Deixe claro o que dói: o que não é possível fazer hoje, o que custa caro, ou o risco que
se corre mantendo as coisas como estão. Uma proposta sem problema demonstrado não
sobrevive à revisão.

## Proposta

O desenho proposto, em detalhe suficiente para que outra pessoa do time consiga
implementá-lo. Nomeie os componentes que serão criados ou alterados, com backticks:
`NomeDoServico`, `/api/v1/rota`, `arquivo.yml`.

Descreva o que muda em cada camada afetada e o que permanece intocado. Se a proposta
altera contratos de API ou schema de banco, mostre o antes e o depois.

## Desvantagens

Por que **não** fazer isso. Custos, complexidade acrescentada, portas que se fecham,
trabalho que a proposta cria para o time.

Esta seção é obrigatória e não aceita "nenhuma". Toda proposta tem custo; não enxergá-lo
é sinal de que a análise está incompleta.

## Alternativas consideradas

As outras opções que estavam sobre a mesa, cada uma com o trade-off explicitado.

- **Nome da alternativa.** O que ela resolveria e o que a torna pior que a proposta.
- **Outra alternativa.** Idem.
- **Não fazer nada.** Sempre vale avaliar manter o comportamento atual e o que custa.

## Questões em aberto

O que ainda não está decidido e precisa da opinião dos revisores. Perguntas concretas
rendem revisão útil; "sugestões são bem-vindas" não rende nada.

- Pergunta que precisa de resposta antes de implementar.
- Ponto do desenho em que o autor está genuinamente dividido.

## Possibilidades futuras

O que esta proposta viabiliza mais adiante, sem se comprometer a fazer agora. Serve para
mostrar que o desenho não fecha portas, e para separar o que está no escopo do que não
está.

## Decisão registrada

Preenchida ao final do período de comentários.

- **Resultado**: Aceita | Rejeitada | Adiada
- **Data**: AAAA-MM-DD
- **Aprovada por**: @usuarios-github de quem aprovou
- **ADR gerado**: [ADR-XXXX](../adr/XXXX-titulo.md), ou "nenhum" quando a RFC é
  rejeitada ou adiada, com uma linha explicando o motivo.