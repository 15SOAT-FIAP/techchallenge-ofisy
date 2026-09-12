# RFC-NNNN. Título curto da proposta

Data: DD/MM/AAAA
Autor: @usuario-github de quem propõe

## Status

Rascunho | Aberta para comentários | Revisada | Encerrada - Aprovada | Encerrada - Rejeitada | Cancelada

Quando a proposta for substituída por outra, registre aqui:
Substituída por [RFC-XXXX](XXXX-titulo.md).

## Resumo

Breve descrição da proposta. O objetivo principal deve ficar claro em até 3 linhas. Quem
ler só esta seção precisa entender o que está sendo pedido e por quê.

## Problema

O cenário atual, descrito em fatos e no presente: a limitação técnica, o desafio ou a dor
que motivou a proposta. Cite o comportamento atual do sistema e, quando houver, o ADR que
registrou a decisão que se pretende revisar.

Deixe claro o que dói: o que não é possível fazer hoje, o que custa caro, ou o risco que
se corre mantendo as coisas como estão. Uma proposta sem problema demonstrado não
sobrevive à revisão.

Seja específico, mas evite aprofundar na solução nesta seção, ela vem a seguir.

## Proposta Técnica

O desenho proposto, em detalhe suficiente para que outra pessoa do time consiga
implementá-lo. Nomeie os componentes que serão criados ou alterados, com backticks:
`NomeDoServico`, `/api/v1/rota`, `arquivo.yml`.

Pode conter diagramas, pseudocódigo ou fluxos, desde que ajudem a ilustrar a proposta de
forma clara. Indique como ela se encaixa na arquitetura atual.

Descreva o que muda em cada camada afetada e o que permanece intocado. Se a proposta
altera contratos de API ou schema de banco, mostre o antes e o depois.

## Impacto esperado

Os benefícios, riscos, impactos operacionais, custos e restrições técnicas que podem
surgir com a implementação da proposta.

**Benefícios.** O que a proposta resolve e o que ela viabiliza, ancorado no problema
descrito acima.

**Riscos e custos.** Por que **não** fazer isso: complexidade acrescentada, portas que se
fecham, trabalho que a proposta cria para o time.

A parte de riscos e custos é obrigatória e não aceita "nenhum". Toda proposta tem custo;
não enxergá-lo é sinal de que a análise está incompleta.

## Alternativas consideradas

As outras opções que estavam sobre a mesa, cada uma com o trade-off explicitado.

- **Nome da alternativa.** O que ela resolveria e o que a torna pior que a proposta.
- **Outra alternativa.** Idem.
- **Não fazer nada.** Sempre vale avaliar manter o comportamento atual e o que custa.

## Pontos em aberto

Quais decisões ou aspectos ainda precisam de alinhamento. Liste tópicos que precisam de
validação, estudo ou decisão coletiva.

Perguntas concretas rendem revisão útil; "sugestões são bem-vindas" não rende nada.

- Pergunta que precisa de resposta antes de implementar.
- Ponto do desenho em que o autor está genuinamente dividido.

## Decisão registrada

Preenchida ao final do período de comentários.

- **Resultado**: Encerrada - Aprovada | Encerrada - Rejeitada | Cancelada
- **Data**: DD/MM/AAAA
- **Aprovada por**: @usuarios-github de quem aprovou
- **ADR gerado**: [ADR-XXXX](../adr/XXXX-titulo.md), ou "nenhum" quando a RFC é
  rejeitada ou cancelada, com uma linha explicando o motivo.