# 0009. Registrar notificações de forma síncrona no banco

Data: 2026-09-06

## Status

Aceito

## Contexto

O sistema precisa avisar sobre dois eventos: quando o estoque de um produto cai abaixo do
mínimo e quando um orçamento é gerado para uma ordem de serviço. O primeiro interessa ao
almoxarife, o segundo ao cliente, que precisa aprovar ou reprovar.

As opções avaliadas:

- **Registro síncrono no próprio banco**, dentro da transação que originou o evento, exposto
  por endpoints de leitura. Simples, sem infraestrutura adicional, e a notificação nasce
  consistente com o dado que a originou. Em contrapartida, acopla a notificação ao caminho
  crítico da operação e não entrega nada por conta própria: alguém precisa consultar.
- **Publicação em mensageria**, com SNS, SQS ou EventBridge, e um consumidor despachando o
  aviso. Desacopla a notificação da transação, absorve pico e permite acrescentar canais sem
  tocar no domínio, ao custo de introduzir mensageria, entrega ao menos uma vez com
  necessidade de idempotência e consistência eventual.
- **Eventos de aplicação do Spring**, com `@TransactionalEventListener`. Desacopla dentro do
  processo e dispara só após o commit, mas continua tudo no monolito e some com o evento se
  a aplicação cair entre o commit e o processamento.

O volume de eventos é baixo e os dois casos existentes são avisos operacionais, não
disparadores de fluxo. Nada no comportamento atual do sistema exige entrega assíncrona ou
garantia de despacho.

## Decisão

Vamos registrar as notificações como linhas no banco, gravadas de forma síncrona dentro da
transação que originou o evento.

`ConsumeStockService` verifica, após salvar o estoque, se ele ficou abaixo do mínimo e nesse
caso chama `CreateLowStockNotificationUseCase`. `GenerateServiceOrderQuoteService` faz o
equivalente para o orçamento. Ambos os serviços são `@Transactional`, de modo que a
notificação é persistida na mesma transação da operação de negócio.

Não há envio ativo: nenhum e-mail, SMS ou push. A notificação é um registro consultado sob
demanda por `/api/v1/notifications`, com rotas para listar por tipo, filtrar as não lidas e
marcar como lida. As rotas de notificação de ordem de serviço estão entre as protegidas pelo
authorizer no API Gateway, o que permite ao cliente consultar as próprias notificações.

## Consequências

- (+) A notificação é atômica com o evento que a originou: se a transação de consumo de
  estoque falhar, não sobra aviso de um consumo que não aconteceu.
- (+) Nenhuma infraestrutura adicional é necessária, o que mantém o custo em créditos do
  laboratório e a complexidade operacional baixos.
- (+) O histórico de notificações fica consultável e auditável, com estado de leitura.
- (+) O fluxo é fácil de testar, porque não depende de fila nem de serviço externo.
- (-) A notificação está no caminho crítico da operação: uma falha ao gravá-la desfaz a
  transação inteira e impede o consumo de estoque ou a geração do orçamento, ainda que o aviso
  seja acessório em relação à operação.
- (-) Não há entrega ativa. O interessado só descobre o evento se consultar a API.
- (-) Os casos de uso de estoque e de ordem de serviço dependem diretamente dos casos de uso
  de notificação, acoplamento que precisará ser desfeito caso o envio saia da aplicação.
- (-) Acrescentar um canal de entrega, como e-mail ou push, exigirá revisar esta decisão, já
  que o desenho atual não tem ponto de extensão para isso.