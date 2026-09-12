# 0004. Utilizar a AWS como provedor de nuvem

Data: 2026-09-06

## Status

Aceito

## Contexto

O time passou a usar nuvem já na Fase 2, quando isso ainda não era cobrado pelo Tech
Challenge. A Fase 3 exige infraestrutura em nuvem, mas deixa o provedor a critério do
grupo: são obrigatórios um banco de dados gerenciado, um cluster Kubernetes com
escalabilidade, uma function serverless para autenticação, um API Gateway e o
provisionamento via Terraform, sem que nenhum fornecedor específico seja imposto.

A escolha, portanto, foi do time nas duas fases. GCP com GKE e Azure com AKS oferecem
serviços equivalentes para tudo o que o projeto precisa, e nenhuma diferença técnica entre
eles seria decisiva neste contexto. O que pesou foi a FIAP disponibilizar o **AWS Academy
Learner Lab** com créditos para o grupo: usar outro provedor significaria arcar com o custo
da infraestrutura do próprio bolso. Antecipar a adoção na Fase 2 também permitiu que o time
chegasse à Fase 3 com o ambiente e a esteira de deploy já em funcionamento.

O Learner Lab, por outro lado, não é uma conta AWS comum. Ele impõe limitações que afetam o
desenho da solução: as credenciais são temporárias e expiram em algumas horas, as IAM Roles
têm nomes gerados dinamicamente (`LabRole`, `LabEksClusterRole`, `LabEksNodeRole`) e a
criação de policies novas é restrita. Essas limitações precisam ser absorvidas pela
arquitetura, não contornadas.

## Decisão

Vamos utilizar a AWS como provedor de nuvem, na região `us-east-1`, com a infraestrutura
provisionada via Terraform.

Os serviços adotados:

- **EKS** para o cluster Kubernetes gerenciado, na versão 1.29, com node group de instâncias
  `t3.medium`.
- **RDS PostgreSQL** como banco gerenciado, em `db.t3.micro`, alocado em subnets privadas,
  com criptografia em repouso e retenção de backup de sete dias.
- **ECR** como registro das imagens Docker da aplicação, configurado com
  `image_tag_mutability = IMMUTABLE`.
- **VPC** própria em `10.0.0.0/16`, com subnets públicas para os nodes e o load balancer e
  subnets privadas para o RDS, distribuídas entre as zonas `us-east-1a` e `us-east-1b`.
- **API Gateway HTTP** e **Lambda** para a autenticação de clientes, detalhados no
  [ADR-0006](0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md).

O isolamento é feito por security group: o RDS aceita conexões na porta 5432 apenas a partir
do security group do EKS.

Como consequência direta das restrições do Learner Lab, o serviço usa a anotação nativa
`service.beta.kubernetes.io/aws-load-balancer-type: nlb` para provisionar um Network Load
Balancer, em vez de instalar o AWS Load Balancer Controller, que exigiria uma IAM policy que
o ambiente não libera. Esse NLB é interno
(`service.beta.kubernetes.io/aws-load-balancer-internal: "true"`): o acesso externo entra
pelo API Gateway e chega até ele por VPC Link.

## Consequências

- (+) O projeto roda dentro dos créditos fornecidos pela FIAP, sem custo para o grupo.
- (+) EKS, RDS e ECR cobrem cluster, banco e registro de imagens sem exigir operação manual
  de servidores.
- (+) A infraestrutura descrita em Terraform pode ser destruída e recriada, o que é
  necessário porque o laboratório é reiniciado com frequência.
- (+) Ter adotado a nuvem antes de ela ser cobrada deu ao time familiaridade com o ambiente
  e uma esteira de deploy pronta quando a Fase 3 passou a exigi-la.
- (-) As credenciais temporárias expiram em poucas horas, obrigando a atualizar os secrets
  `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN` no GitHub antes de cada
  novo deploy pela pipeline.
- (-) As IAM Roles têm nomes gerados dinamicamente pelo laboratório, o que exige descobri-las
  via AWS CLI antes de aplicar o Terraform em vez de fixá-las na configuração.
- (-) A restrição de criar policies impede o uso do AWS Load Balancer Controller e, com ele,
  de recursos de Ingress mais elaborados.
- (-) A solução fica acoplada a serviços gerenciados da AWS. Migrar para outro provedor
  exigiria reescrever o Terraform, ainda que a aplicação em si permaneça portável por rodar
  em contêiner.