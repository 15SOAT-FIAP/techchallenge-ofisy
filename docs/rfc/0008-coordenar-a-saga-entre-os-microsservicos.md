# RFC-0008. Coordenar a saga entre os microsserviços

Data: 03/10/2026
Autor: @rogerbertan

## Status

Encerrada - Aprovada

## Resumo

Usar SNS com SQS, em tópicos e filas FIFO, como mensageria entre os microsserviços, e
coordenar a saga da OS por coreografia: cada serviço reage aos eventos dos outros, sem um
orquestrador. Os eventos saem de um outbox gravado na mesma transação do negócio.

## Problema

O [ADR-0010](../adr/0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md)
dividiu a aplicação em `core`, `ms-ofisy-billing`, `ms-ofisy-stock`, `ms-ofisy-execution` e
`ms-ofisy-notification`, cada um com banco próprio, e definiu que o fluxo da OS é uma saga
por mensageria. Ficaram em aberto duas decisões: qual tecnologia transporta os eventos e
quem coordena os passos da saga.

Sem essas respostas, nenhum serviço pode ser extraído. O fluxo descrito na
[RFC-0007](0007-dividir-a-aplicacao-em-microsservicos.md) vai do orçamento à entrega,
passando por estoque, execução e pagamento, e hoje cada passo é uma chamada de método dentro
de uma transação, como `CreateQuoteService` chamando `ConsumeStockUseCase`.

Há também um problema que aparece com qualquer mensageria. Com um banco por serviço, gravar
a mudança de estado e publicar o evento são duas operações em sistemas diferentes, que não
cabem na mesma transação. Se o serviço grava e cai antes de publicar, o resto da saga nunca
fica sabendo; se publica e a transação falha, os outros reagem a algo que não aconteceu.

## Proposta Técnica

**Mensageria: SNS com SQS.** Cada serviço que produz eventos publica em um tópico SNS
próprio, com os eventos do seu contexto. Cada serviço que consome tem uma única fila SQS,
assinada nos tópicos que lhe interessam, com filter policy pelo tipo do evento para receber
só os eventos que trata. Cada fila tem a sua dead-letter queue. O `ms-ofisy-notification` só
consome e não tem tópico.

**FIFO.** Tópicos e filas são FIFO. O identificador da OS é o grupo de mensagens, de modo
que os eventos de uma mesma OS são entregues na ordem em que aconteceram, enquanto OS
diferentes são processadas em paralelo. Eventos que não pertencem a uma OS, como o de estoque
baixo, usam o id do agregado de origem. O id do evento é a chave de deduplicação.

**DLQ.** Cada fila envia para a sua DLQ a mensagem que ultrapassa o número máximo de
tentativas. Uma mensagem que falha repetidas vezes sai do caminho em vez de travar a fila.

**Transactional outbox.** Nenhum serviço publica no SNS dentro do caso de uso. O caso de uso
grava o evento em uma tabela de outbox do próprio banco, na mesma transação da mudança de
estado. Um publicador agendado lê os registros pendentes, publica no SNS e os marca como
publicados. Se o serviço cair entre publicar e marcar, o evento é publicado de novo e a
deduplicação FIFO o descarta. O outbox existe nos serviços que publicam eventos; o
`ms-ofisy-notification` só consome e não precisa dele.

**Envelope.** Todo evento carrega o mesmo envelope, com id do evento, tipo, id da OS, data
de ocorrência e payload. O tipo é o atributo usado nas filter policies, e o id do evento é o
que permite aos consumidores descartar repetições.

**Saga por coreografia.** Nenhum serviço comanda os outros. Cada serviço publica fatos do
seu próprio contexto, no passado, como "orçamento aprovado" ou "estoque consumido", e nunca
ordens para outro serviço. Quem se interessa por um fato assina o tópico e decide como
reagir. O fluxo da RFC-0007 se forma pela sequência dessas reações, e as compensações
também: um serviço desfaz a própria parte ao receber um evento de falha, de reprovação ou de
cancelamento.

Os nomes dos tópicos, das filas e dos eventos ficam para a implementação de cada serviço e
não fazem parte desta decisão.

**O core como visão da saga.** O core é dono de `ServiceOrderStatus` e consome os eventos
dos outros serviços para mantê-lo atualizado. Ele não envia ordens a ninguém, mas o status
da OS passa a ser o lugar onde se vê em que passo a saga está, o que resolve a principal
dificuldade da coreografia sem criar um orquestrador.

**Ambiente local e infraestrutura.** Tópicos, filas, assinaturas, filter policies e DLQs são
declarados no Terraform. Localmente e nos testes de integração, o SNS e o SQS rodam no
LocalStack, já usado no `techchallenge-ofisy-auth`.

## Impacto esperado

**Benefícios.**

- Tudo continua na AWS, como decidido no
  [ADR-0004](../adr/0004-utilizar-a-aws-como-provedor-de-nuvem.md). SNS e SQS são
  gerenciados e cobrados por uso, sem broker para dimensionar, atualizar ou manter no ar.
- O LocalStack já faz parte do fluxo de trabalho do time, de modo que a mensageria pode ser
  exercitada localmente e nos testes sem conta na AWS.
- O fan-out do SNS permite que um mesmo evento chegue a vários serviços sem que quem publica
  saiba quem consome.
- Sem orquestrador, não há um serviço central que precise conhecer todos os outros nem um
  ponto único de falha para a saga.
- Um serviço novo passa a reagir a um evento existente assinando o tópico, sem alterar quem
  o publica.
- O outbox garante que nenhum evento se perde e que nenhum evento é publicado por uma
  transação que falhou.
- O agrupamento por OS garante que os eventos de uma mesma ordem chegam na ordem em que
  aconteceram.

**Riscos e custos.**

- A lógica do fluxo fica espalhada pelos serviços. Não existe um lugar no código onde a saga
  inteira esteja escrita, e entender o fluxo exige ler os consumidores de cada serviço. O
  status da OS no core mitiga, mas não substitui essa visão.
- As compensações também ficam distribuídas: cada serviço precisa saber desfazer a própria
  parte ao receber um evento de falha ou de cancelamento.
- Na coreografia, um evento pode provocar outro que leva à mesma reação. Uma reprovação de
  orçamento que devolve os itens e cancela a OS, por exemplo, gera um cancelamento que pede a
  devolução dos mesmos itens de novo. As compensações precisam ser idempotentes.
- O teste ponta a ponta da saga exige todos os serviços e a mensageria no ar.
- FIFO tem vazão menor e custo maior que filas padrão, e um tópico SNS FIFO só entrega para
  filas SQS FIFO.
- Uma mensagem presa na DLQ deixa a OS parada no passo em que estava. As DLQs precisam de
  alarme e de um procedimento de reprocessamento.
- A deduplicação FIFO vale por uma janela de 5 minutos. Uma publicação repetida pelo outbox
  depois disso chega duas vezes, e os consumidores ainda precisam ser idempotentes.
- O outbox acrescenta, em cada serviço produtor, uma tabela, um publicador agendado e o
  atraso do intervalo de leitura antes de cada evento sair.
- SNS e SQS são serviços proprietários da AWS. Trocar de nuvem significa trocar a
  mensageria e reescrever os adaptadores de publicação e consumo.

## Alternativas consideradas

- **Kafka, gerenciado pelo Amazon MSK.** Log persistente com reprocessamento e ordenação por
  partição. É mais do que o volume da oficina pede, custa caro mesmo parado e é mais pesado
  de subir localmente.
- **RabbitMQ, gerenciado pelo Amazon MQ.** Roteamento flexível e protocolo aberto, mas é um
  broker com instância para dimensionar e manter, que o SNS com SQS dispensa.
- **EventBridge.** Roteamento por regras e integração com outros serviços da AWS, mas não
  garante a ordem dos eventos, que o fluxo da OS precisa.
- **SQS sem SNS**, com cada produtor enviando direto para as filas dos consumidores. Remove
  um componente, mas o produtor passa a conhecer cada consumidor, e acrescentar um novo
  exige alterar quem publica.
- **Saga orquestrada pelo core**, com filas de comando para cada serviço. Deixa o fluxo
  inteiro escrito em um lugar só, mas faz do core um coordenador que conhece todos os
  serviços e precisa guardar o estado de cada saga, além do status da OS.
- **Saga orquestrada por Step Functions.** Dá a visão do fluxo pronta, mas é mais um serviço
  para manter, com definição em linguagem própria e emulação limitada no LocalStack.
- **Filas padrão com consumidores idempotentes**, em vez de FIFO. Mais vazão e menor custo,
  mas sem ordem garantida, os eventos de uma mesma OS podem chegar trocados, como a
  conclusão das execuções antes do início.
- **Publicar no SNS logo após o commit**, sem outbox. Mais simples, mas um evento se perde se
  o serviço cair entre o commit e a publicação, e a saga fica parada sem que ninguém saiba.
- **Não fazer nada.** Sem mensageria e sem coordenação definidas, nenhum serviço pode ser
  extraído, e o ADR-0010 não sai do papel.

## Pontos em aberto

- Filas FIFO ou padrão? A ordem por OS compensa a menor vazão e o custo maior, dado que os
  consumidores precisarão ser idempotentes de qualquer forma?
- Com a coreografia, como alguém descobre em que passo uma OS parou? O status no core basta,
  ou é preciso algo mais?
- O publicador do outbox lê a tabela por polling, ou vale usar CDC, lendo o log do
  PostgreSQL?
- Quem reprocessa as mensagens da DLQ, e como? Manualmente, ou com uma rotina automática?

## Decisão registrada

- **Resultado**: Encerrada - Aprovada
- **Data**: 03/10/2026
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**:
  [ADR-0011](../adr/0011-utilizar-sns-e-sqs-como-mensageria.md) e
  [ADR-0012](../adr/0012-coordenar-a-saga-por-coreografia.md)

SNS com SQS venceu por manter tudo na AWS, sem broker para operar, e por poder ser testado
no LocalStack que o time já usa. A coreografia foi aceita porque o fluxo da OS é linear e
tem poucos participantes, e porque o core já é dono do status da OS, o que dá à saga um
lugar onde ela pode ser acompanhada sem um orquestrador.

As questões em aberto foram resolvidas assim: as filas são FIFO, com agrupamento por OS,
porque a ordem dos eventos de uma ordem importa mais que a vazão no volume da oficina, e os
consumidores continuam idempotentes; o status da OS no core é a visão da saga, e o id da OS
vai nos logs de todos os serviços para rastrear uma OS entre eles; o outbox é lido por
polling, para não acrescentar infraestrutura de CDC; e o reprocessamento da DLQ é manual
nesta fase, com alarme quando uma mensagem chega nela.