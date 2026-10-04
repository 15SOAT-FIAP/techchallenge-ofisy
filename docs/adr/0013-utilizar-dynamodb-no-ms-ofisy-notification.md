# 0013. Utilizar DynamoDB no ms-ofisy-notification

Data: 2026-10-03

## Status

Aceito

## Contexto

A Fase 4 exige que pelo menos um dos microsserviços use um banco NoSQL. O
[ADR-0010](0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md) deu a cada
serviço um banco próprio, e toda a infraestrutura do projeto está na AWS, como decidido no
[ADR-0004](0004-utilizar-a-aws-como-provedor-de-nuvem.md).

O `ms-ofisy-notification` é o serviço em que esse requisito se encaixa com menos atrito:

- Os acessos são simples e conhecidos. Hoje `NotificationRepository` busca por id, por tipo,
  por status de leitura e tipo, e lista tudo. Não há junção nem agregação.
- Depois do recorte, as notificações não têm relação com nenhuma outra tabela do próprio
  serviço. A chave estrangeira para `stocks` deixa de existir com o banco por serviço.
- Os atributos variam conforme o tipo. `stock_id` e `quote_id` são opcionais e foram
  acrescentados por migrations à medida que surgiram tipos novos (`V14` e `V21`).
- A escrita é contínua, a cada evento consumido, e a leitura se concentra nas notificações
  recentes.
- O serviço só consome eventos e não publica nenhum, como definido no
  [ADR-0011](0011-utilizar-sns-e-sqs-como-mensageria.md). Por isso não precisa do outbox
  transacional, que seria mais difícil de implementar fora de um banco relacional.

Os outros serviços têm transações e invariantes que se beneficiam de um banco relacional:
saldo de estoque, itens e valores de orçamento, transições de estado da OS. Além disso,
todos publicam eventos pelo outbox.

As opções avaliadas:

- **Amazon DocumentDB.** Modelo de documentos compatível com MongoDB, mas exige instância
  para dimensionar e manter, com custo fixo mesmo sem uso.
- **MongoDB Atlas.** Mesmo modelo de documentos, mas fora da AWS, com rede, credenciais e
  cobrança separadas do resto da infraestrutura.
- **ElastiCache com Redis.** Rápido para leitura, mas pensado como cache, não como
  armazenamento primário durável das notificações.
- **Amazon Keyspaces.** Compatível com Cassandra, com modelagem orientada a consultas mais
  pesada do que o volume da oficina justifica.
- **NoSQL em outro serviço**, como estoque ou orçamento. Perderia as transações que esses
  contextos precisam e complicaria o outbox.
- **Manter PostgreSQL no `ms-ofisy-notification`.** Funcionaria, mas não atende ao requisito
  da fase.

## Decisão

Vamos usar DynamoDB, em modo de capacidade sob demanda, como banco do
`ms-ofisy-notification`.

A modelagem parte dos padrões de acesso do serviço, com índice secundário para as consultas
por tipo e por status de leitura. Nomes de tabela, chaves e índices são definidos na
implementação.

A tabela é declarada no Terraform. Localmente e nos testes de integração, o DynamoDB roda no
LocalStack, junto com o SNS e o SQS.

Os demais serviços continuam em PostgreSQL.

## Consequências

- (+) O requisito de banco NoSQL da fase é atendido em um serviço cujo modelo de dados se
  encaixa nele.
- (+) É gerenciado e cobrado por uso, sem instância para dimensionar ou manter.
- (+) Escala com o volume de escrita sem ajuste manual.
- (+) Um tipo novo de notificação com atributos próprios não exige migration.
- (+) O TTL nativo permite expirar notificações antigas sem rotina de limpeza.
- (+) O mesmo LocalStack usado para a mensageria cobre o DynamoDB localmente e nos testes.
- (-) Os padrões de acesso precisam ser conhecidos antes de modelar. Uma consulta nova pode
  exigir um índice novo ou remodelar a tabela, e não há consulta ad hoc como em SQL.
- (-) Leituras em índice secundário são eventualmente consistentes: uma notificação recém
  marcada como lida pode aparecer como não lida por um instante.
- (-) Paginação e filtragem funcionam de forma diferente do Spring Data JPA usado nos demais
  serviços.
- (-) O Flyway do [ADR-0007](0007-versionar-o-schema-com-flyway.md) não se aplica. A
  estrutura da tabela passa a viver no Terraform e no código.
- (-) Os testes de integração desse serviço usam LocalStack em vez do PostgreSQL do
  Testcontainers definido no [ADR-0008](0008-testar-integracao-com-testcontainers.md).
- (-) É mais uma tecnologia de persistência para o time dominar.
- (-) Os dados atuais da tabela `notifications` precisam ser migrados do PostgreSQL para o
  DynamoDB na extração do serviço.
- (-) Aumenta a dependência de serviços proprietários da AWS.