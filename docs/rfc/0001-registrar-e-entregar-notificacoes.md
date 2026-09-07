# RFC-0001. Registrar e entregar notificações

Data: 2026-09-04
Autor: @rogerbertan

## Status

Aceita

## Resumo

O sistema precisa avisar sobre dois eventos: quando o estoque de um produto cai abaixo do
mínimo e quando um orçamento é gerado para uma ordem de serviço. Esta RFC propõe registrar
as notificações de forma síncrona no próprio banco, dentro da transação que originou o
evento, expondo-as por endpoints de leitura, sem entrega ativa nesta fase.

## Motivação

Dois pontos do sistema produzem informação que alguém precisa receber, e hoje não há
nenhum mecanismo para isso.

O primeiro é o consumo de estoque. Quando `ConsumeStockService` dá baixa em um produto e a
quantidade fica abaixo do mínimo configurado, o almoxarife precisa saber para repor. Sem
aviso, a falta só aparece quando um serviço não pode ser executado.

O segundo é a geração de orçamento. `GenerateServiceOrderQuoteService` produz o orçamento
de uma ordem de serviço, e o cliente precisa aprová-lo ou reprová-lo para que o trabalho
siga. Enquanto ele não souber que existe um orçamento, a ordem fica parada.

São dois interessados diferentes, com dois tipos de aviso diferentes, e ambos precisam de
um lugar onde o evento fique registrado. A questão em aberto é onde esse registro vive e
se o sistema deve despachar o aviso ativamente ou apenas disponibilizá-lo para consulta.

## Proposta

Registrar as notificações como linhas no banco, gravadas de forma síncrona dentro da
transação que originou o evento.

**Domínio.** Um agregado `Notification`, com `NotificationType` distinguindo os dois casos
e `NotificationMessage` como objeto de valor para o texto do aviso, seguindo a Clean
Architecture do [ADR-0002](../adr/0002-adotar-clean-architecture-com-ddd.md).

**Criação.** `ConsumeStockService` verifica, após salvar o estoque, se ele ficou abaixo do
mínimo e nesse caso chama `CreateLowStockNotificationUseCase`.
`GenerateServiceOrderQuoteService` faz o equivalente para o orçamento, chamando
`CreateQuoteNotificationUseCase`. Ambos os serviços são `@Transactional`, de modo que a
notificação é persistida na mesma transação da operação de negócio.

**Consulta.** As notificações são expostas em `/api/v1/notifications`, com rotas para
buscar por identificador, listar por tipo, filtrar as não lidas e marcar como lida,
separadas entre `/stock` e `/service-orders`. As rotas de notificação de ordem de serviço
entram entre as protegidas pelo authorizer no API Gateway, o que permite ao cliente
consultar as próprias notificações, conforme a separação de autenticação do
[ADR-0006](../adr/0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md).

**Entrega.** Não há envio ativo nesta fase: nenhum e-mail, SMS ou push. A notificação é um
registro consultado sob demanda.

## Desvantagens

- A notificação passa a estar no caminho crítico da operação. Uma falha ao gravá-la desfaz
  a transação inteira e impede o consumo de estoque ou a geração do orçamento, ainda que o
  aviso seja acessório em relação à operação.
- Não há entrega ativa. O interessado só descobre o evento se consultar a API, o que
  transfere para ele a responsabilidade de lembrar de olhar.
- Os casos de uso de estoque e de ordem de serviço passam a depender diretamente dos casos
  de uso de notificação, acoplamento que precisará ser desfeito caso o envio saia da
  aplicação.
- Acrescentar um canal de entrega mais adiante exigirá revisar esta decisão, já que o
  desenho proposto não tem ponto de extensão para isso.

## Alternativas consideradas

- **Publicação em mensageria**, com SNS, SQS ou EventBridge, e um consumidor despachando o
  aviso. Desacopla a notificação da transação, absorve pico e permite acrescentar canais
  sem tocar no domínio, ao custo de introduzir mensageria, entrega ao menos uma vez com
  necessidade de idempotência e consistência eventual. É o desenho mais robusto, mas
  consome créditos do laboratório e exige Terraform novo para um ganho que o volume atual
  não justifica.
- **Eventos de aplicação do Spring**, com `@TransactionalEventListener`. Desacopla dentro
  do processo e dispara só após o commit, sem infraestrutura nova. Continua tudo no
  monolito e some com o evento se a aplicação cair entre o commit e o processamento. Com o
  HPA do [ADR-0005](../adr/0005-escalar-a-aplicacao-com-hpa-por-cpu.md), pods sendo
  encerrados é o comportamento normal, não a exceção.
- **Padrão outbox**, gravando na transação e despachando por um publicador assíncrono.
  Resolve o caminho crítico e viabiliza entrega ativa preservando a atomicidade, mas
  acrescenta tabela de controle, publicador, coordenação entre réplicas e integração de
  canal. É trabalho considerável para dois avisos operacionais de baixo volume.
- **Não notificar nada**, deixando que o almoxarife e o cliente descubram os eventos pelas
  telas existentes. Nenhum custo, mas deixa a ordem de serviço parada esperando uma
  aprovação que o cliente não sabe que precisa dar.

## Questões em aberto

- A entrega ativa é requisito desta fase? Se for, a proposta muda: o outbox passa a ser o
  desenho mínimo, porque despachar dentro da transação não é opção.
- Vale já gravar as notificações com um campo de estado de entrega, mesmo sem publicador,
  para não precisar de migração depois? Ou é otimização prematura para algo que pode não
  acontecer?
- As duas notificações têm o mesmo ciclo de vida, ou a de orçamento precisa de estados
  próprios (aprovado, reprovado) que a de estoque não tem?

## Possibilidades futuras

- Acrescentar entrega ativa por e-mail ou push, migrando para o padrão outbox sem perder o
  histórico já registrado.
- Migrar para mensageria na Fase 4, se o projeto seguir para microsserviços como o
  [ADR-0002](../adr/0002-adotar-clean-architecture-com-ddd.md) aponta. O registro em banco
  é o passo que torna essa migração segura, porque o que precisa ser entregue já está
  persistido.
- Preferência de canal por destinatário, uma vez que exista mais de um canal.

## Decisão registrada

- **Resultado**: Aceita
- **Data**: 2026-09-06
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**:
  [ADR-0009](../adr/0009-registrar-notificacoes-de-forma-sincrona-no-banco.md)

O volume de eventos é baixo e os dois casos existentes são avisos operacionais, não
disparadores de fluxo. Nada no comportamento atual do sistema exige entrega assíncrona ou
garantia de despacho, o que tornou o registro síncrono a opção adequada para a fase.

As questões em aberto foram resolvidas assim: a entrega ativa não é requisito desta fase;
o campo de estado de entrega não será criado antes de existir publicador; e as duas
notificações compartilham o mesmo ciclo de vida, com o estado de leitura sendo o único
controle necessário.
