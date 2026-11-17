# 0015. Ajustar a saga da OS ao fluxo implementado

Data: 2026-11-16

## Status

Aceito

## Contexto

O [ADR-0010](0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md) descreveu o
fluxo da OS como uma saga, com os passos e as compensações que a
[RFC-0007](../rfc/0007-dividir-a-aplicacao-em-microsservicos.md) propôs, e restringiu o REST
síncrono à consulta do catálogo pelo `ms-ofisy-billing`. O
[ADR-0011](0011-utilizar-sns-e-sqs-como-mensageria.md) definiu que as mensagens que chegam à
DLQ geram alarme.

Na extração dos serviços, o fluxo mudou em vários pontos, por decisões tomadas na
implementação de cada um:

- O orçamento não nasce de um evento da OS em diagnóstico. Quem monta o orçamento é o
  atendente, que escolhe os serviços e as peças, e por isso a criação é uma chamada à API do
  `ms-ofisy-billing`.
- O estoque não reserva itens: dá baixa já na criação do orçamento. Não existe no estoque um
  saldo reservado separado do disponível, e a devolução desfaz a baixa.
- O orçamento pode ser editado antes da aprovação, o que a RFC não previa.
- A aprovação e a reprovação são ações do cliente sobre o orçamento, e portanto rotas do
  `ms-ofisy-billing`, não do core.
- Um orçamento que não pode ser atendido ou que foi reprovado não deixa a OS com nada a
  fazer, e mantê-la aberta exigiria um novo orçamento que o fluxo não oferece.
- A entrega do veículo é uma ação do atendente, que não acontece no mesmo instante da
  confirmação do pagamento.
- O Checkout Pro do Mercado Pago aceita Pix, cartão e boleto, e uma order recusada pode ser
  substituída por outra sem intervenção. Uma order expirada indica que o cliente não pagou no
  prazo.
- O `ms-ofisy-billing` precisa do nome e do preço das peças para montá-las no orçamento, e o
  `ms-ofisy-notification` precisa saber de quem é a OS para restringir as notificações ao
  cliente dono dela. Nenhum dos dois dados vem nos eventos.
- O alarme de DLQ não foi criado. O reprocessamento manual já cobre o volume da oficina, e as
  DLQs são acompanhadas pelos comandos de inspeção de cada serviço.

## Decisão

Vamos manter o fluxo da saga como implementado, por coreografia e com os eventos abaixo,
substituindo os passos e as compensações descritos no ADR-0010.

**Caminho feliz.**

1. O atendente cria o orçamento no `ms-ofisy-billing` (`POST /quotes`). Com peças, o billing
   publica `QuoteCreated`; só com serviços, publica direto `QuoteSentForApproval`.
2. O `ms-ofisy-stock` dá baixa nas peças e publica `StockConsumed`. O billing envia o
   orçamento para aprovação e publica `QuoteSentForApproval`, que leva a OS a
   `AWAITING_APPROVAL` e gera a notificação ao cliente.
3. Uma edição do orçamento (`PATCH /quotes/{id}`) publica `QuoteUpdated`. O estoque baixa o
   que aumentou, devolve o que diminuiu e responde com `StockConsumed` ou
   `StockConsumptionFailed`, e o orçamento volta a ser enviado para aprovação.
4. O cliente aprova o orçamento no billing, que publica `QuoteApproved`. O
   `ms-ofisy-execution` cria as execuções e o core move a OS para `AWAITING_EXECUTION`.
5. O `ms-ofisy-execution` publica `ExecutionStarted` na primeira execução iniciada e
   `AllExecutionsCompleted` na última concluída, e o core move a OS para `IN_PROGRESS` e
   depois para `FINISHED`, publicando `ServiceOrderFinished`.
6. O billing cria a order no Mercado Pago (Checkout Pro) e publica `PaymentCheckoutCreated`.
   O webhook do Mercado Pago, que é rota pública validada pela assinatura, leva a cobrança a
   `PAID` e o billing publica `PaymentConfirmed`.
7. O core move a OS para o status novo `PAID`, e o atendente entrega o veículo
   (`PATCH /service-orders/{id}/deliver`), que só é aceito a partir de `PAID`.

**Compensações.**

- **Falta de saldo**: o estoque publica `StockConsumptionFailed`, o billing cancela o
  orçamento e publica `QuoteCancelled`, e o core cancela a OS.
- **Orçamento reprovado**: o billing publica `QuoteReproved`, o estoque devolve as peças e
  publica `StockRefunded`, e o core cancela a OS. O core não assina o `QuoteReproved`.
- **OS cancelada pela API do core**: o core publica `ServiceOrderCancelled`, o
  `ms-ofisy-execution` cancela as execuções pendentes e em andamento e publica
  `AllExecutionsCancelled`, o estoque devolve as peças e publica `StockRefunded`, e o billing
  cancela o orçamento ainda aberto. Quando a OS é cancelada pelo `QuoteCancelled` ou pelo
  `StockRefunded`, o core não publica `ServiceOrderCancelled`, porque ainda não há
  execuções.
- **Pagamento recusado**: a cobrança vai a `FAILED`, o billing publica `PaymentFailed` e cria
  na hora uma nova order, com um novo `PaymentCheckoutCreated`.
- **Pagamento expirado ou cancelado no Mercado Pago**: a cobrança vai a `EXPIRED` e o billing
  publica `PaymentFailed`, sem gerar nova cobrança. A OS permanece em `FINISHED`, com a
  entrega bloqueada.

**REST síncrono.** Além da consulta do catálogo no core pelo `ms-ofisy-billing`, ficam
permitidas duas leituras:

- `ms-ofisy-billing` → `ms-ofisy-stock`, em `GET /api/v1/stocks/{id}`, para copiar o nome e o
  preço da peça para o item do orçamento na criação e na edição;
- `ms-ofisy-notification` → core, em `GET /api/v1/service-orders/{id}/customer`, com token
  de serviço, para associar a notificação de orçamento ao cliente dono da OS.

**DLQ.** As mensagens que chegam à DLQ não geram alarme nesta fase. A inspeção e o
reprocessamento são manuais, pelos comandos de cada serviço, substituindo o alarme previsto
no ADR-0011.

## Consequências

- (+) A documentação de arquitetura passa a descrever o fluxo que de fato roda, e o ADR-0010
  continua valendo para as fronteiras entre os serviços.
- (+) Sem reserva, o estoque tem um único saldo, e a devolução é a mesma operação para
  reprovação e cancelamento.
- (+) O cliente aceita qualquer meio de pagamento do Checkout Pro, e uma recusa não exige
  ação do atendente para gerar um novo link.
- (+) O status `PAID` separa a confirmação do pagamento da entrega física do veículo.
- (-) As peças saem do estoque antes da aprovação. Um orçamento esperando aprovação deixa
  essas peças indisponíveis para outras OS até ser aprovado, reprovado ou cancelado.
- (-) Falta de saldo e reprovação encerram a OS. Para continuar o atendimento, é preciso
  abrir uma OS nova.
- (-) Uma OS com cobrança expirada fica em `FINISHED` sem caminho para uma nova cobrança.
- (-) O `ms-ofisy-billing` depende do core e do `ms-ofisy-stock` no ar para criar e editar
  orçamentos, e o `ms-ofisy-notification` depende do core para registrar a notificação de
  orçamento.
- (-) O `ms-ofisy-notification` passa a guardar um token de serviço do core, mais um segredo a
  distribuir e rotacionar.
- (-) Sem alarme, uma mensagem na DLQ só é percebida quando alguém consulta a fila ou nota
  uma OS parada.