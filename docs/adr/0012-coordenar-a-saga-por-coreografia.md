# 0012. Coordenar a saga por coreografia

Data: 2026-10-03

## Status

Aceito

## Contexto

O [ADR-0010](0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md) definiu que
o fluxo da OS é uma saga entre `core`, `ms-ofisy-billing`, `ms-ofisy-stock` e
`ms-ofisy-execution`, com compensações para orçamento reprovado, OS cancelada, falta de
estoque e pagamento recusado, e deixou para uma decisão própria quem coordena os passos. A
mensageria escolhida no [ADR-0011](0011-utilizar-sns-e-sqs-como-mensageria.md) é SNS com
SQS, com fan-out por tópico.

O fluxo é linear, do orçamento à entrega, e tem quatro participantes. O core já é dono de
`ServiceOrderStatus` e precisa acompanhar cada passo para mantê-lo atualizado.

As opções avaliadas:

- **Orquestração pelo core**, com filas de comando para cada serviço. Deixa o fluxo escrito
  em um lugar só, mas faz do core um coordenador que conhece todos os serviços e guarda o
  estado de cada saga, além do status da OS.
- **Orquestração por Step Functions.** Dá a visão do fluxo pronta, mas é mais um serviço
  para manter, com definição em linguagem própria e emulação limitada no LocalStack.
- **Coreografia**, com cada serviço reagindo aos eventos dos outros. Combina com o fan-out
  do SNS e dispensa coordenador, ao custo de espalhar o fluxo entre os serviços.

## Decisão

Vamos coordenar a saga por coreografia.

Cada serviço publica fatos do seu próprio contexto, no passado, e nunca ordens para outro
serviço. Quem se interessa por um fato assina o tópico e decide como reagir. As compensações
seguem a mesma regra: cada serviço desfaz a própria parte ao receber um evento de falha, de
reprovação ou de cancelamento, e toda compensação é idempotente.

O core consome os eventos dos outros serviços para manter o status da OS, que passa a ser a
visão de em que passo a saga está. Ele não envia ordens a ninguém. O id da OS vai nos logs
de todos os serviços para rastrear uma OS entre eles.

## Consequências

- (+) Não há um serviço central que conheça todos os outros nem um ponto único de falha para
  a saga.
- (+) Um serviço novo passa a reagir a um evento existente assinando o tópico, sem alterar
  quem o publica.
- (+) O status da OS no core mostra em que passo a saga está, sem um orquestrador guardando
  estado à parte.
- (-) Não existe um lugar no código onde a saga inteira esteja escrita. Entender o fluxo
  exige ler os consumidores de cada serviço.
- (-) As compensações ficam distribuídas, e cada serviço precisa saber desfazer a própria
  parte.
- (-) Um evento pode provocar outro que leva à mesma reação, como a reprovação de orçamento
  que cancela a OS e gera um segundo pedido de devolução dos mesmos itens. As compensações
  precisam ser idempotentes para isso não causar efeito duplicado.
- (-) Testar a saga de ponta a ponta exige todos os serviços e a mensageria no ar.