# Mensageria

O core se comunica com os microsserviços por eventos, usando **Amazon SNS + SQS** ([ADR-0011](adr/0011-utilizar-sns-e-sqs-como-mensageria.md)), com a saga coordenada por coreografia ([ADR-0012](adr/0012-coordenar-a-saga-por-coreografia.md)). Os eventos publicados pelo core saem por um *transactional outbox* para o tópico `ofisy-service-order-events.fifo`, e os eventos dos outros serviços chegam por uma única fila FIFO, `ofisy-service-order.fifo`.

A estrutura é a mesma do `ms-ofisy-execution`: mesmo envelope, mesmo outbox e mesmo despacho por `type`. Assim, um evento publicado por qualquer um dos lados tem o mesmo contrato.

---

## Topologia

```mermaid
flowchart LR
    app[core] -->|outbox + publisher| topic[(SNS<br/>ofisy-service-order-events.fifo)]
    topic -.->|só local, raw delivery| inspect[(SQS<br/>ofisy-service-order-events-inspect.fifo)]
    queue[(SQS<br/>ofisy-service-order.fifo)] --> app
    queue -->|após 5 recebimentos| dlq[(SQS<br/>ofisy-service-order-dlq.fifo)]
```

| Recurso          | Nome                                      | Observação                                                            |
|------------------|-------------------------------------------|-----------------------------------------------------------------------|
| Tópico           | `ofisy-service-order-events.fifo`         | Tópico do core, configurado em `ofisy.messaging.topics.service-order` |
| Fila             | `ofisy-service-order.fifo`                | Configurada em `ofisy.messaging.queues.service-order`                 |
| DLQ              | `ofisy-service-order-dlq.fifo`            | `RedrivePolicy` com `maxReceiveCount=5`                               |
| Fila de inspeção | `ofisy-service-order-events-inspect.fifo` | Só local. Assinada no tópico do core para ver o que foi publicado     |

Ainda não há assinaturas da fila em tópicos de outros serviços: elas entram junto com o primeiro handler de cada evento.

Localmente, tudo é criado pelo `localstack/init/ready.d/01-messaging.sh`, que roda quando o LocalStack sobe (pelo `compose.yaml`, pelo `compose.db.yaml` e nos testes de integração). Com `queue-not-found-strategy: fail`, a aplicação não inicia se a fila não existir.

Na AWS, os recursos vêm do repositório de infraestrutura. O tópico é configurado pelo **nome**: o `SnsConfig` registra um `TopicsListingTopicArnResolver`, que descobre o ARN listando os tópicos (`sns:ListTopics`) em vez de chamar `CreateTopic`, e guarda o resultado em cache. A aplicação precisa de `sns:ListTopics`, `sns:Publish` e, na fila, `sqs:GetQueueUrl`, `sqs:ReceiveMessage`, `sqs:DeleteMessage` e `sqs:ChangeMessageVisibility`.

---

## Contrato dos eventos

Todo evento segue o mesmo envelope (`adapters/listeners/dto/EventEnvelope.java`). O `payload` varia conforme o `type`:

| Campo            | Tipo    | Descrição                                                   |
|------------------|---------|-------------------------------------------------------------|
| `eventId`        | UUID    | Identificador único do evento, usado para idempotência      |
| `type`           | String  | Tipo do evento, usado no filtro da assinatura e no despacho |
| `serviceOrderId` | UUID    | Ordem de serviço à qual o evento se refere                  |
| `occurredAt`     | Instant | Momento em que o evento ocorreu no produtor                 |
| `payload`        | JSON    | Dados específicos do tipo de evento                         |

```json
{
  "eventId": "5e6f7a8b-9c0d-4e1f-8a2b-3c4d5e6f7a8b",
  "type": "ServiceOrderCancelled",
  "serviceOrderId": "c3d4e5f6-a7b8-9012-cdef-123456780001",
  "occurredAt": "2026-10-20T10:00:00Z",
  "payload": {}
}
```

### Eventos consumidos

| Evento | Produtor | Efeito |
|--------|----------|--------|
| —      | —        | —      |

### Eventos publicados

| Evento | Quando é publicado | Quem reage |
|--------|--------------------|------------|
| —      | —                  | —          |

---

## Consumo de eventos

O `ServiceOrderEventsListener` (`adapters/listeners/`) escuta a fila `ofisy-service-order.fifo`. Ele não conhece os eventos: recebe todos os `EventHandler` registrados no Spring e despacha pelo `type`. Cada tipo de evento tem seu próprio handler em `adapters/listeners/handlers/`.

- **Tipo sem handler**: o evento é logado como ignorado e confirmado, sem passar pelo banco.
- **Mensagem inválida ou erro no processamento**: a exceção impede o ack, a mensagem volta para a fila depois do *visibility timeout* e, depois de 5 recebimentos, vai para a DLQ.
- **Ordenação**: as mensagens chegam com `message-group-id = serviceOrderId`, então eventos de uma mesma OS são processados em ordem e OSs diferentes em paralelo.
- **Idempotência**: a entrega do SQS é *at-least-once*. O caso de uso acionado pelo handler registra o `eventId` em **`processed_events`** (`domain/messaging/ProcessedEventRepository`) na mesma transação do seu efeito e descarta o evento se o `eventId` já existir.

### Como adicionar um novo evento consumido

1. Criar o DTO do payload em `adapters/listeners/dto/`.
2. Criar um `@Component` em `adapters/listeners/handlers/` implementando `EventHandler`, com `type()` retornando o nome do evento.
3. No caso de uso acionado pelo handler, checar e gravar o `eventId` em `processed_events` na mesma transação.
4. Criar a assinatura da fila no tópico do produtor, com `RawMessageDelivery=true` e `FilterPolicy` pelo `type` (`FilterPolicyScope=MessageBody`), em `localstack/init/ready.d/01-messaging.sh` e na infraestrutura da AWS.
5. Cobrir com teste unitário do handler e, se fizer sentido, com um cenário no `ServiceOrderEventsListenerIT`.
6. Adicionar a linha na tabela de [eventos consumidos](#eventos-consumidos).

---

## Publicação de eventos (outbox)

Os casos de uso não chamam o SNS: eles gravam o evento na tabela `outbox_events`, na mesma transação da mudança de estado, e o `OutboxEventPublisher` é quem publica no tópico, de forma agendada, seguindo a [RFC-0008](rfc/0008-coordenar-a-saga-entre-os-microsservicos.md). Se a transação falhar, o evento não é gravado; se o SNS estiver fora, o evento espera no banco.

```mermaid
sequenceDiagram
    participant UC as Caso de uso
    participant DB as PostgreSQL
    participant P as OutboxEventPublisher
    participant SNS as SNS ofisy-service-order-events.fifo

    UC->>DB: muda o estado + insert outbox_events (mesma transação)
    loop a cada publish-interval
        P->>DB: select pendentes for update, em ordem de occurred_at
        loop cada evento do lote
            P->>SNS: envelope (groupId = serviceOrderId, deduplicationId = eventId)
            P->>DB: published_at = now()
        end
    end
```

| Peça                                                    | Papel                                                                  |
|---------------------------------------------------------|------------------------------------------------------------------------|
| `domain/messaging/OutboxEvent`                          | O evento a publicar: tipo, OS e payload. Id e `occurredAt` são gerados |
| `domain/messaging/OutboxEventRepository`                | Porta usada pelos casos de uso. Só tem `save`                          |
| `adapters/gateways/messaging/OutboxEventRepositoryImpl` | Serializa o payload para JSON e grava em `outbox_events`               |
| `adapters/publishers/OutboxEventPublisher`              | Lê os pendentes, publica o envelope no SNS e marca `published_at`      |

As entidades de `outbox_events` e `processed_events` sempre inserem no `save` (`Persistable.isNew() = true`). O id vem do domínio ou do evento, então não há consulta prévia para decidir entre persist e merge, e um evento repetido viola a PK.

### Garantias

- **Ordem**: os pendentes são publicados em ordem de `occurred_at`, com `message-group-id = serviceOrderId`. Quando um evento falha, o publicador loga e **pula os eventos seguintes da mesma OS** naquele lote, para não publicá-los fora de ordem. Os eventos das outras OSs continuam sendo publicados. O evento que falhou é tentado de novo no próximo ciclo.
- **Evento que sempre falha**: um evento com falha permanente é tentado a cada ciclo e segura os eventos seguintes da sua OS, mas não bloqueia as outras OSs. Ainda não há limite de tentativas nem status de erro.
- **Mais de uma réplica**: a busca dos pendentes usa `SELECT ... FOR UPDATE`. Uma segunda réplica espera o lote da primeira terminar e já encontra os eventos marcados.
- **Duplicidade**: se a aplicação cair entre publicar e gravar `published_at`, o evento é publicado de novo. Dentro da janela de 5 minutos, a deduplicação FIFO (`deduplicationId = eventId`) descarta a cópia; depois disso, os consumidores precisam ser idempotentes pelo `eventId`.
- **Retenção**: os eventos publicados continuam na tabela, com `published_at` preenchido. Ainda não há limpeza automática.

### Configuração

| Propriedade                               | Padrão                            | Descrição                                               |
|-------------------------------------------|-----------------------------------|---------------------------------------------------------|
| `ofisy.messaging.queues.service-order`    | `ofisy-service-order.fifo`        | Fila de onde o core consome os eventos                  |
| `ofisy.messaging.topics.service-order`    | `ofisy-service-order-events.fifo` | Tópico onde os eventos do core saem                     |
| `ofisy.messaging.outbox.publish-interval` | `1s`                              | Intervalo entre o fim de um ciclo e o início do próximo |
| `ofisy.messaging.outbox.batch-size`       | `100`                             | Máximo de eventos publicados por ciclo                  |

### Como publicar um novo evento

1. Criar um `record` com o payload do evento.
2. No caso de uso, dentro da transação que muda o estado, chamar `outboxEventRepository.save(OutboxEvent.create("NomeDoEvento", serviceOrderId, payload))`.
3. Cobrir no teste do caso de uso que o evento é gravado e adicionar a linha na tabela de [eventos publicados](#eventos-publicados).

---

## Testes

Os testes de integração (`integration/IntegrationTestBase`) sobem um LocalStack via Testcontainers com o mesmo `01-messaging.sh` do compose, então a fila, a DLQ e o tópico são os mesmos do ambiente local. A espera é pelo log `Ready.`, que o LocalStack imprime depois que o init script termina.

| Teste                          | O que cobre                                                                              |
|--------------------------------|------------------------------------------------------------------------------------------|
| `OutboxEventPublisherTest`     | Envelope FIFO, ordem, `published_at` e o salto dos eventos da OS que falhou              |
| `ServiceOrderEventsListenerTest` | Despacho pelo `type`, tipo sem handler, falha do handler e `type` duplicado entre handlers |
| `OutboxEventPublisherIT`       | Evento gravado no outbox chega na fila de inspeção e fica com `published_at`             |
| `ServiceOrderEventsListenerIT` | Evento sem handler é confirmado e sai da fila                                            |

---

## Exercitando o fluxo localmente

Com o LocalStack no ar (`docker compose -f compose.db.yaml up -d`):

```bash
# eventos publicados pelo core
docker exec ofisy-local-localstack awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/ofisy-service-order-events-inspect.fifo \
  --max-number-of-messages 10

# enviar um evento direto para a fila do core
docker exec ofisy-local-localstack awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/ofisy-service-order.fifo \
  --message-group-id c3d4e5f6-a7b8-9012-cdef-123456780001 \
  --message-deduplication-id $(uuidgen) \
  --message-body '{"eventId":"'$(uuidgen)'","type":"ExecutionStarted","serviceOrderId":"c3d4e5f6-a7b8-9012-cdef-123456780001","occurredAt":"2026-10-17T13:00:00Z","payload":{}}'
```

Sem handler para `ExecutionStarted`, o log mostra `ServiceOrderEventsListener.onMessage >> ignoring event ...` e a mensagem sai da fila.

> O LocalStack do core e o do `ms-ofisy-execution` usam a porta `4566`, então só um deles pode ficar no ar por vez. Para exercitar a comunicação entre os dois serviços, os dois precisam apontar para o mesmo LocalStack, com os tópicos e as assinaturas de ambos.