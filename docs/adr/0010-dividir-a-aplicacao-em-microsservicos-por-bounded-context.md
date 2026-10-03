# 0010. Dividir a aplicação em microsserviços por bounded context

Data: 2026-10-02

## Status

Aceito

## Contexto

O Ofisy é um monolito modular, com todos os agregados em uma única aplicação e um único
banco PostgreSQL. A Fase 4 pede a evolução para microsserviços, com banco de dados próprio
para cada serviço e o padrão saga para coordenar os fluxos que atravessam mais de um deles.
A organização do [ADR-0002](0002-adotar-clean-architecture-com-ddd.md) deixou as fronteiras
entre agregados explícitas para que a extração fosse um recorte por pacote.

As transações, porém, atravessam contextos: `CreateQuoteService` consome estoque e cria as
execuções do orçamento na mesma transação, o estoque e a OS registram notificações chamando
os casos de uso de `notification` diretamente, como decidido no
[ADR-0009](0009-registrar-notificacoes-de-forma-sincrona-no-banco.md), e a OS aprova,
reprova e cancela chamando `quote` e `serviceorderexecution`. Com um banco por serviço,
nenhuma dessas chamadas cabe mais em uma transação.

A integração com o Mercado Pago, também pedida pela fase, ainda não existe.

As opções avaliadas para o recorte:

- **Manter o monolito modular.** Preserva as transações atômicas, mas não atende à fase.
- **Recorte mais fino**, com cliente, veículo e catálogo de serviços em serviços próprios.
  São cadastros simples, sem fluxo, lidos por quase todos os contextos. Separá-los
  multiplicaria as chamadas síncronas sem ganho de autonomia.
- **Catálogo de serviços no serviço de execução.** O catálogo descreve o que o mecânico
  executa, mas o orçamento precisa do preço para ser montado, o que faria o faturamento
  depender de um serviço que só atua depois da aprovação.
- **Orçamento e estoque no mesmo serviço.** Manteria atômica a reserva de itens, mas o
  estoque é usado também fora de orçamentos e tem outro responsável na oficina.
- **Cobrar na aprovação do orçamento.** Eliminaria o risco de inadimplência, mas obrigaria a
  estornar OS canceladas e colocaria o Mercado Pago no caminho crítico da execução.
- **Banco compartilhado** ou **REST síncrono em todo o fluxo.** Mais simples, mas nenhum dos
  dois atende à fase, que exige banco por serviço e saga.

## Decisão

Vamos dividir a aplicação em cinco serviços, recortados pelos bounded contexts que o código
já tem, cada um com repositório, pipeline, banco e ciclo de deploy próprios:

- **`core`**: ciclo de vida da OS e transições de `ServiceOrderStatus`, cadastros de
  cliente, veículo, funcionário e catálogo de serviços, e o login de funcionário.
- **`ms-billing`**: orçamentos (`quote`) e a cobrança via Mercado Pago.
- **`ms-stock`**: saldo e movimentação de estoque (`stock`, `stockmovement`).
- **`ms-execution`**: execução dos serviços da OS pelo mecânico (`serviceorderexecution`).
- **`ms-notification`**: registro e consulta de notificações (`notification`).

Nenhum serviço lê tabela de outro. As referências entre serviços são apenas por id, sem
chave estrangeira para fora do próprio banco, e cada serviço versiona o próprio schema com
Flyway, como no [ADR-0007](0007-versionar-o-schema-com-flyway.md).

O fluxo da OS é uma saga por mensageria: orçamento gerado, itens reservados, orçamento
aprovado, execuções concluídas, OS finalizada, cobrança gerada no Mercado Pago e pagamento
confirmado liberando a entrega. Cada passo tem compensação: orçamento reprovado ou OS
cancelada devolvem os itens reservados e cancelam as execuções pendentes, e pagamento
recusado mantém a OS em `FINISHED` com a entrega bloqueada até uma nova cobrança.

REST síncrono fica restrito à leitura de cadastros do core, como a consulta do preço no
catálogo ao montar o orçamento. A tecnologia de mensageria e a escolha entre saga
orquestrada e coreografada ficam para uma RFC própria.

O API Gateway continua sendo o único ponto de entrada público, e o Lambda Authorizer do
[ADR-0006](0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md) é reaproveitado
nas rotas de cliente de todos os serviços.

Os serviços serão extraídos um por vez, com o monolito mantendo as tabelas até cada corte.

## Consequências

- (+) As fronteiras entre contextos passam a ser físicas: uma dependência indevida deixa de
  ser possível por descuido.
- (+) Cada serviço tem deploy e escala independentes.
- (+) A indisponibilidade do Mercado Pago atrasa apenas a entrega, sem impedir abrir,
  diagnosticar, orçar ou executar OS.
- (+) Cobrar só depois de finalizar evita estorno de OS cancelada no meio do caminho.
- (+) Uma falha em um serviço não derruba os outros: o passo que falha fica pendente ou é
  compensado.
- (+) As notificações deixam de ser acopladas ao código de quem as origina, abrindo caminho
  para a entrega ativa que o ADR-0009 deixou como limitação.
- (-) A consistência passa a ser eventual, e a OS ganha estados intermediários que a API e
  as telas precisam tratar.
- (-) As transações atômicas deixam de existir, incluindo a da notificação com o evento que
  a originou, principal vantagem do ADR-0009.
- (-) Cada compensação é código que só roda no caminho de falha. Um erro nela deixa estoque
  reservado para sempre ou uma OS presa em um estado.
- (-) Mensagens chegam ao menos uma vez, e todos os consumidores precisam ser idempotentes.
- (-) São cinco repositórios, pipelines, bancos e conjuntos de manifestos Kubernetes, com
  custo de AWS e de manutenção crescendo na mesma proporção.
- (-) A observabilidade precisa propagar um identificador de correlação pelas mensagens
  para que um problema possa ser rastreado entre serviços.
- (-) Os dados do banco único precisam ser migrados, e as chaves estrangeiras entre
  contextos deixam de existir.
- (-) A Lambda de autenticação de clientes continua lendo `customers` direto do banco do
  core, e o segredo do JWT passa a ser distribuído para cinco serviços, o que encarece a
  rotação já apontada como dívida no ADR-0006.