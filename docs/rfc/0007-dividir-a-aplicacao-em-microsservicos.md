# RFC-0007. Dividir a aplicação em microsserviços

Data: 02/10/2026
Autor: @rogerbertan

## Status

Encerrada - Aprovada

## Resumo

Extrair do monolito quatro microsserviços, um por bounded context: `ms-billing`,
`ms-stock`, `ms-execution` e `ms-notification`. O contexto de ordem de serviço fica no
core. Cada serviço passa a ter banco próprio, e o fluxo da OS vira uma saga por mensageria.

## Problema

O Ofisy é hoje um monolito modular, com todos os agregados em uma única aplicação e um
único banco PostgreSQL. A Fase 4 pede a evolução para microsserviços, com banco de dados
próprio para cada serviço e o padrão saga para coordenar os fluxos que atravessam mais de
um deles. A organização do
[ADR-0002](../adr/0002-adotar-clean-architecture-com-ddd.md) foi escolhida justamente para
que essa extração fosse um recorte por pacote em vez de uma reescrita. Falta decidir onde
passam os cortes.

A fronteira entre pacotes existe, mas as transações atravessam contextos. Hoje:

- `CreateQuoteService` consome estoque por `ConsumeStockUseCase` e cria as
  `ServiceOrderExecution` do orçamento, tudo na mesma transação.
- `ConsumeStockService` registra a notificação de estoque baixo chamando
  `CreateLowStockNotificationUseCase` diretamente, e `GenerateServiceOrderQuoteService` faz o
  mesmo com a notificação de orçamento, como decidido no
  [ADR-0009](../adr/0009-registrar-notificacoes-de-forma-sincrona-no-banco.md).
- `application/serviceorder` aprova e reprova orçamentos chamando os casos de uso de `quote`,
  e cancela execuções pendentes ao cancelar a OS.

Essas chamadas funcionam porque tudo divide o mesmo processo e o mesmo banco. Com um banco
por serviço, nenhuma delas cabe mais em uma transação, e separar os serviços sem decidir
como esses fluxos continuam consistentes troca uma chamada de método por uma falha parcial
sem tratamento.

O pagamento também não existe hoje. A Fase 4 pede integração com Mercado Pago, e é preciso
decidir em qual serviço ela vive e em que ponto do fluxo da OS a cobrança acontece.

## Proposta Técnica

Dividir a aplicação em cinco serviços, recortados pelos bounded contexts que o código já
tem. Cada um com repositório, pipeline, banco e ciclo de deploy próprios.

| Serviço           | Responsabilidade                          | Agregados atuais                                                |
|-------------------|-------------------------------------------|-----------------------------------------------------------------|
| `core`            | Ciclo de vida da OS e cadastros           | `serviceorder`, `customer`, `vehicle`, `user`, `servicecatalog` |
| `ms-billing`      | Orçamentos e pagamento via Mercado Pago   | `quote`                                                         |
| `ms-stock`        | Saldo e movimentação de estoque           | `stock`, `stockmovement`                                        |
| `ms-execution`    | Execução dos serviços da OS pelo mecânico | `serviceorderexecution`                                         |
| `ms-notification` | Registro e consulta de notificações       | `notification`                                                  |

**Core.** Continua dono da OS e das transições de `ServiceOrderStatus`. Mantém os cadastros
de cliente, veículo, funcionário e catálogo de serviços, além do login de funcionário em
`/api/v1/login`.

**`ms-billing`.** Gera, atualiza, aprova e reprova orçamentos, e integra com o Mercado Pago
para cobrar a OS finalizada. O preço de cada serviço continua sendo copiado do catálogo para
`QuoteServiceItem.price` no momento da criação, de modo que o orçamento não depende do
catálogo depois de criado e a cobrança usa o valor aprovado pelo cliente.

**`ms-stock`.** Mantém o saldo e as movimentações. Os casos de uso `consume` e `release`
viram as operações de reservar e devolver itens de um orçamento.

**`ms-execution`.** Cria as execuções de uma OS a partir do orçamento aprovado e as conduz
por `ServiceOrderExecutionStatus` até `COMPLETED`, incluindo o tempo médio por serviço.

**`ms-notification`.** Registra as notificações a partir dos eventos dos outros serviços,
substituindo as chamadas diretas aos casos de uso de `notification`.

**Dados.** Banco por serviço. Nenhum serviço lê tabela de outro. Referências entre serviços
são apenas por id: o orçamento guarda o `serviceOrderId`, a execução guarda o
`serviceCatalogId`, e nenhum deles tem chave estrangeira para fora do próprio banco. Cada
serviço versiona o próprio schema com Flyway, como no
[ADR-0007](../adr/0007-versionar-o-schema-com-flyway.md).

**Comunicação.** O fluxo da OS passa a ser uma saga por mensageria. O diagrama abaixo mostra
a ordem dos passos e o serviço responsável por cada um, não quem os coordena. Se a saga
será orquestrada ou coreografada fica para a RFC que definirá a mensageria.

```
core           OS em diagnóstico           -> ms-billing gera o orçamento
ms-billing     orçamento gerado            -> ms-stock reserva os itens
ms-stock       itens reservados            -> core move a OS para AWAITING_APPROVAL
core           orçamento aprovado          -> ms-execution cria as execuções
ms-execution   execuções concluídas        -> core move a OS para FINISHED
core           OS finalizada               -> ms-billing gera a cobrança no Mercado Pago
ms-billing     pagamento confirmado        -> core libera a entrega (DELIVERED)
```

Cada passo tem uma compensação para quando o fluxo para no meio:

- orçamento reprovado: `ms-stock` devolve os itens reservados;
- OS cancelada antes de finalizar: `ms-stock` devolve os itens e `ms-execution` cancela as
  execuções pendentes;
- reserva sem saldo: `ms-billing` marca o orçamento como não atendido e o core é avisado;
- pagamento recusado ou expirado: a OS permanece em `FINISHED`, com a entrega bloqueada, e o
  `ms-billing` permite gerar uma nova cobrança. Não há o que devolver, porque o serviço já
  foi executado.

REST síncrono fica restrito à leitura de cadastros do core, em que a resposta é necessária
para completar a operação: `ms-billing` consulta o preço no catálogo ao montar o orçamento.
A escolha da tecnologia de mensageria não faz parte desta RFC e terá uma RFC própria.

**Entrada e autenticação.** O API Gateway continua sendo o único ponto de entrada público e
passa a rotear para cada serviço. O Lambda Authorizer de clientes do
[ADR-0006](../adr/0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md) é
reaproveitado nas rotas de cliente de todos os serviços. O JWT de funcionário continua
emitido pelo core e validado por cada serviço com o mesmo segredo.

## Impacto esperado

**Benefícios.**

- As fronteiras que o ADR-0002 deixou explícitas no código passam a ser físicas: uma
  dependência indevida entre contextos deixa de ser possível por descuido.
- Cada serviço tem deploy e escala independentes. O `ms-stock` e o `ms-execution`, mais
  consultados no dia a dia da oficina, escalam sem levar o resto junto.
- A integração com o Mercado Pago fica isolada no `ms-billing` e no fim do fluxo. Uma
  indisponibilidade do provedor de pagamento atrasa apenas a entrega, sem impedir abrir,
  diagnosticar, orçar ou executar OS.
- Cobrar só depois de finalizar evita estorno: uma OS cancelada no meio do caminho nunca
  chegou a ser cobrada.
- Uma falha em um serviço não derruba os outros. Com a saga, um passo que falha fica
  pendente ou é compensado, em vez de desfazer a operação inteira.
- As notificações deixam de ser acopladas ao código de quem as origina, abrindo caminho para
  entrega ativa, que o ADR-0009 deixou como limitação.

**Riscos e custos.**

- A consistência passa a ser eventual. Entre um passo e outro da saga a OS fica em estados
  intermediários que hoje não existem, e as telas e a API precisam lidar com eles.
- As transações atômicas de hoje deixam de existir. A notificação deixa de ser atômica com o
  evento que a originou, que era a principal vantagem do ADR-0009, e a reserva de estoque
  deixa de ser atômica com a criação do orçamento.
- Cada compensação é código novo que só roda no caminho de falha, o menos exercitado. Um
  erro em uma delas deixa estoque reservado para sempre ou uma OS presa em um estado.
- Mensagens chegam ao menos uma vez. Todos os consumidores precisam ser
  idempotentes, incluindo o que recebe a confirmação de pagamento do Mercado Pago.
- São cinco repositórios, cinco pipelines, cinco bancos e cinco conjuntos de manifestos
  Kubernetes. O custo de infraestrutura na AWS e o trabalho de manutenção crescem na mesma
  proporção.
- Rastrear um problema passa a exigir correlacionar logs e traces de vários serviços. A
  observabilidade atual, pensada para um processo só, precisa propagar um identificador de
  correlação pelas mensagens.
- Os dados do banco único precisam ser migrados para os bancos de cada serviço, e as chaves
  estrangeiras entre contextos, como de `quote_service_items` para
  `service_order_executions`, deixam de existir.
- A Lambda de autenticação de clientes continua lendo `customers` direto do banco do core.
  O acoplamento registrado no ADR-0006 permanece, agora com um serviço específico.
- O segredo compartilhado do JWT passa a ser distribuído para cinco serviços em vez de um,
  o que torna mais cara a rotação já apontada como dívida no ADR-0006.

## Alternativas consideradas

- **Manter o monolito modular.** Preserva as transações atômicas e o custo operacional
  atual, mas não atende à Fase 4.
- **Recorte mais fino**, com cliente, veículo e catálogo de serviços em serviços próprios.
  São cadastros simples, sem fluxo, lidos por quase todos os outros contextos. Separá-los
  multiplica as chamadas síncronas sem ganho de autonomia.
- **Catálogo de serviços no `ms-execution`.** O catálogo descreve o que o mecânico executa,
  e o tempo médio por serviço já é calculado lá. Descartado porque o `ms-billing` precisa do
  preço para montar o orçamento, o que o faria depender de um serviço que só atua depois da
  aprovação.
- **Orçamento e estoque no mesmo serviço.** Mantém atômica a reserva de itens com a criação
  do orçamento. Descartado porque o estoque é usado também fora de orçamentos, pela entrada
  de mercadoria e pela movimentação do almoxarife, e misturaria dois contextos com
  responsáveis diferentes na oficina.
- **Cobrar na aprovação do orçamento**, antes da execução. Elimina o risco de inadimplência,
  mas obriga a estornar quando a OS é cancelada depois do pagamento e coloca o Mercado Pago
  no caminho crítico do início da execução.
- **Banco compartilhado entre os serviços.** Evitaria a migração de dados e preservaria as
  chaves estrangeiras, mas não atende à Fase 4, que exige banco próprio para cada serviço.
  Além disso, qualquer mudança de schema voltaria a acoplar os deploys, que é o que a
  divisão quer eliminar.
- **REST síncrono em todo o fluxo**, com o core chamando cada serviço em sequência. Seria
  mais simples de entender e de testar, mas não atende à Fase 4, que exige o padrão saga.
  Além disso, a indisponibilidade de qualquer serviço interromperia o fluxo inteiro, e uma
  falha no meio deixaria os serviços anteriores sem compensação.
- **Não fazer nada.** Não atende ao requisito da fase.

## Pontos em aberto

- Qual tecnologia de mensageria usar? A escolha afeta garantias de entrega, ordenação e
  custo, e pode merecer uma RFC própria em vez de ser decidida aqui.
- A saga deve ser orquestrada pelo core, que conhece o estado da OS, ou coreografada, com
  cada serviço reagindo aos eventos dos outros sem um coordenador? A resposta depende da
  mensageria escolhida e pode ser decidida junto com ela.
- Como migrar os dados do banco único: corte de uma vez, ou extração de um serviço por vez
  com o monolito ainda dono das tabelas durante a transição?

## Decisão registrada

- **Resultado**: Encerrada - Aprovada
- **Data**: 02/10/2026
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**:
  [ADR-0010](../adr/0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md)

O recorte seguiu os bounded contexts que o código já tinha, o que faz da extração um
recorte por pacote, como previsto no ADR-0002. Banco por serviço e saga são exigências da
fase; o que esta RFC decide é onde passam as fronteiras e como a saga compensa cada falha.

As questões em aberto foram resolvidas assim: a tecnologia de mensageria fica para uma RFC
própria; a escolha entre saga orquestrada e coreografada fica para essa mesma RFC, porque
depende da tecnologia escolhida; e os serviços serão extraídos um por vez, com o monolito
mantendo as tabelas até cada corte.