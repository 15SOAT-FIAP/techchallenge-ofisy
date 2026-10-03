# 0011. Utilizar SNS e SQS como mensageria

Data: 2026-10-03

## Status

Aceito

## Contexto

O [ADR-0010](0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md) dividiu a
aplicação em cinco serviços com banco próprio e definiu que o fluxo da OS é uma saga por
mensageria, deixando a tecnologia para uma decisão própria.

A mensageria precisa entregar um mesmo evento a mais de um serviço, preservar a ordem dos
eventos de uma mesma OS e rodar localmente e nos testes. Toda a infraestrutura do projeto já
está na AWS, como decidido no [ADR-0004](0004-utilizar-a-aws-como-provedor-de-nuvem.md), e
o LocalStack já é usado no `techchallenge-ofisy-auth`.

Com um banco por serviço, gravar a mudança de estado e publicar o evento não cabem na mesma
transação. Se o serviço cai entre uma operação e outra, um evento se perde ou é publicado
sem que o estado tenha mudado.

As opções avaliadas:

- **Kafka, gerenciado pelo Amazon MSK.** Log persistente com reprocessamento e ordenação por
  partição, mas é mais do que o volume da oficina pede, custa caro mesmo parado e é mais
  pesado de subir localmente.
- **RabbitMQ, gerenciado pelo Amazon MQ.** Roteamento flexível, mas é um broker com
  instância para dimensionar e manter.
- **EventBridge.** Roteamento por regras, mas não garante a ordem dos eventos.
- **SQS sem SNS.** Remove um componente, mas obriga o produtor a conhecer cada consumidor.
- **SNS com SQS em filas padrão.** Mais vazão e menor custo, mas sem ordem garantida.
- **Publicar no SNS logo após o commit**, sem outbox. Mais simples, mas perde o evento se o
  serviço cair entre o commit e a publicação.

## Decisão

Vamos usar SNS com SQS, em tópicos e filas FIFO.

Cada serviço que produz eventos publica em um tópico SNS próprio. Cada serviço que consome
tem uma única fila SQS, assinada nos tópicos que lhe interessam, com filter policy pelo tipo
do evento, e uma dead-letter queue própria. O identificador da OS é o grupo de mensagens, e o
id do evento é a chave de deduplicação.

Os eventos são publicados por transactional outbox: o caso de uso grava o evento em uma
tabela de outbox na mesma transação da mudança de estado, e um publicador agendado, lendo a
tabela por polling, envia ao SNS e marca o registro como publicado. Todo evento tem o mesmo
envelope, com id, tipo, id da OS, data de ocorrência e payload.

Tópicos, filas, assinaturas e DLQs ficam no Terraform. Localmente e nos testes, SNS e SQS
rodam no LocalStack. As mensagens que chegam à DLQ geram alarme e são reprocessadas
manualmente nesta fase.

Os nomes de tópicos, filas e eventos são definidos na implementação de cada serviço.

## Consequências

- (+) Tudo continua na AWS, com mensageria gerenciada e cobrada por uso, sem broker para
  operar.
- (+) A mensageria pode ser exercitada localmente e nos testes com o LocalStack.
- (+) O fan-out do SNS entrega um evento a vários serviços sem que o produtor conheça os
  consumidores.
- (+) Os eventos de uma mesma OS chegam na ordem em que aconteceram.
- (+) O outbox garante que nenhum evento se perde e que nenhum é publicado por uma transação
  que falhou.
- (-) FIFO tem vazão menor e custo maior que filas padrão, e um tópico SNS FIFO só entrega
  para filas SQS FIFO.
- (-) A deduplicação FIFO vale por 5 minutos, e os consumidores continuam precisando ser
  idempotentes.
- (-) O outbox acrescenta tabela, publicador agendado e o atraso do polling a cada serviço
  produtor.
- (-) Uma mensagem na DLQ deixa a OS parada até o reprocessamento manual.
- (-) SNS e SQS são proprietários da AWS: trocar de nuvem exige trocar a mensageria e
  reescrever os adaptadores.