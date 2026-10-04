# 0014. Manter a infraestrutura de bancos em um único repositório

Data: 2026-10-03

## Status

Aceito

## Contexto

O [ADR-0010](0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md) deu a cada
microsserviço um banco próprio, e o
[ADR-0013](0013-utilizar-dynamodb-no-ms-ofisy-notification.md) colocou o
`ms-ofisy-notification` no DynamoDB. Passam a existir cinco bancos: uma instância RDS
PostgreSQL para cada um de `core`, `ms-ofisy-billing`, `ms-ofisy-stock` e
`ms-ofisy-execution`, e uma tabela DynamoDB para o `ms-ofisy-notification`.

A infraestrutura do projeto está dividida em repositórios por camada. O
`techchallenge-ofisy-eks-infra` cuida de rede, cluster, API Gateway e da Lambda de
autenticação, e o `techchallenge-ofisy-rds-infra` cuida do banco. Este último tem hoje uma
única raiz Terraform, com um único state e um único banco, usado pelo monolito.

A nova infraestrutura de dados precisa de duas coisas que puxam em direções opostas:

- **Deploy e destroy independentes por banco.** Os serviços serão extraídos um por vez, e
  provisionar ou alterar o banco de um deles não pode arriscar os bancos dos outros.
- **Não multiplicar repositórios.** Cada repositório traz secrets de AWS, bucket de state,
  pipelines e documentação próprios para manter, em um time de quatro pessoas.

O `techchallenge-ofisy-eks-infra` já resolve um problema igual. Ele tem três raízes no mesmo
repositório (`infra/`, `infra-auth/` e `api-gateway/`), cada uma com sua chave de state no
mesmo bucket e seus próprios workflows de plan, deploy e destroy.

As opções avaliadas:

- **Um repositório por banco.** Isolamento máximo, de código e de permissão, mas são cinco
  repositórios com secrets, pipelines e documentação duplicados, e o código da instância
  PostgreSQL copiado quatro vezes.
- **Um repositório com um único state para todos os bancos.** É o mais simples, mas qualquer
  `apply` toca todos os bancos, e um `destroy` derruba todos de uma vez.
- **O banco no repositório de cada microsserviço.** Aproxima o banco de quem o usa, mas
  acopla a infraestrutura ao pipeline de deploy da aplicação e espalha pelos repositórios as
  regras de rede que liberam o acesso ao banco.
- **Os bancos dentro do `techchallenge-ofisy-eks-infra`.** Reaproveita um repositório que já
  existe, mas mistura o ciclo de vida dos dados com o da rede e do cluster e aumenta o
  impacto de um erro nesse repositório.

## Decisão

Vamos manter toda a infraestrutura de bancos em um único repositório Terraform, renomeando o
`techchallenge-ofisy-rds-infra` para `techchallenge-ofisy-db-infra`, já que ele passa a
conter também o DynamoDB.

O repositório terá uma raiz por serviço: `core`, `billing`, `stock`, `execution` e
`notification`. Cada raiz tem sua própria chave de state no bucket já existente e seus
próprios workflows de plan, deploy e destroy, seguindo o padrão do
`techchallenge-ofisy-eks-infra`.

As quatro instâncias PostgreSQL são criadas a partir de um módulo compartilhado dentro do
próprio repositório, que cada raiz instancia com os seus parâmetros. A raiz `notification`
declara a tabela DynamoDB.

O banco que existe hoje passa a ser o do `core`, e o seu state é preservado, para não
recriar a instância que o monolito continua usando enquanto os serviços são extraídos, como
previsto na [RFC-0007](../rfc/0007-dividir-a-aplicacao-em-microsservicos.md).

Os security groups continuam sendo criados no `techchallenge-ofisy-eks-infra` e localizados
por tag, como hoje.

## Consequências

- (+) Cada banco tem deploy e destroy isolados: um erro no `apply` de um serviço não alcança
  os bancos dos outros.
- (+) Toda a infraestrutura de dados fica em um só lugar, com secrets, bucket de state,
  padrão de pipeline e documentação da ordem de execução compartilhados.
- (+) O módulo PostgreSQL compartilhado evita copiar a definição da instância, e uma
  correção nele vale para todos os bancos.
- (+) O time já conhece o padrão de várias raízes em um repositório, usado no
  `techchallenge-ofisy-eks-infra`.
- (-) Uma alteração no módulo compartilhado afeta as quatro raízes PostgreSQL, e cada uma
  precisa de um `apply` próprio para recebê-la.
- (-) O repositório passa a ter um conjunto de workflows por raiz, com bastante repetição
  entre eles.
- (-) O isolamento é de state, não de permissão: quem pode alterar o repositório pode
  alterar qualquer banco.
- (-) A renomeação exige atualizar remotes locais, links e referências nos READMEs e
  workflows dos outros repositórios. O GitHub redireciona o nome antigo, mas não para
  sempre.
- (-) O state atual precisa ser movido para a raiz `core` sem que o Terraform recrie a
  instância, o que exige cuidado na migração.
- (-) Quatro instâncias RDS em vez de uma multiplicam o custo de banco na AWS.