# Infraestrutura AWS com Terraform - Ofisy

A infraestrutura do Ofisy não fica neste repositório. Ela é provisionada via Terraform em dois repositórios próprios, e o ambiente inteiro é montado e destruído pelo [ofisy-platform](https://github.com/15SOAT-FIAP/ofisy-platform):

| Repositório | O que provisiona |
|---|---|
| [`techchallenge-ofisy-eks-infra`](https://github.com/15SOAT-FIAP/techchallenge-ofisy-eks-infra) | Rede, um cluster EKS e um ECR por serviço, Datadog Agent, Lambdas de autenticação, mensageria (SNS e SQS) e API Gateway |
| [`techchallenge-ofisy-db-infra`](https://github.com/15SOAT-FIAP/techchallenge-ofisy-db-infra) | Um RDS PostgreSQL por serviço e a tabela DynamoDB do notification ([ADR-0014](adr/0014-manter-a-infraestrutura-de-bancos-em-um-unico-repositorio.md)) |

Este documento resume o que existe e o que o core usa. Os comandos para provisionar e destruir cada camada estão nos READMEs desses repositórios. O desenho da arquitetura está em [INFRA-CICD-DIAGRAM.md](INFRA-CICD-DIAGRAM.md).

---

## Recursos usados pelo core

| Recurso | Nome | Camada |
|---|---|---|
| Cluster EKS | `ms-ofisy-core-cluster`, com node group `t3.medium` (1 nó) e Datadog Agent via Helm | `eks-infra/eks/core` |
| ECR | `ms-ofisy-core-ecr`, com tags imutáveis | `eks-infra/eks/core` |
| RDS PostgreSQL 15 | `ms-ofisy-core-db`, banco `ms_ofisy_core_db`, `db.t3.micro` | `db-infra/core` |
| Tópico SNS FIFO | `ofisy-service-order-events.fifo`, onde o core publica | `eks-infra/messaging` |
| Fila SQS FIFO | `ofisy-service-order.fifo`, com a DLQ `ofisy-service-order-dlq.fifo` | `eks-infra/messaging` |
| NLB interno | Criado pelo `Service` `ofisy-service` do `k8s/service.yml`, fora do Terraform | este repositório |
| API Gateway | Rota `ANY /{proxy+}` e `GET /api/v1/service-orders/{id}/status` (com authorizer) para o NLB do core | `eks-infra/api-gateway` |

Os outros serviços têm o mesmo conjunto, com o nome do serviço no lugar de `core`, menos o notification, que usa a tabela DynamoDB `Notifications` em vez de um RDS. As assinaturas entre tópicos e filas estão em [MESSAGING.md](MESSAGING.md) e no README do `eks-infra`.

---

## Arquitetura de rede

```text
AWS Cloud (us-east-1)
 └── VPC (10.0.0.0/16)
      ├── Internet Gateway & NAT Gateway
      ├── Subnet Pública A (10.0.1.0/24 - us-east-1a) ── [NAT Gateway]
      ├── Subnet Pública B (10.0.4.0/24 - us-east-1b)
      ├── Subnet Privada A (10.0.2.0/24 - us-east-1a) ── [Nós dos 5 clusters / NLBs internos / RDS / Lambda de auth / VPC Link]
      └── Subnet Privada B (10.0.3.0/24 - us-east-1b) ── [Nós dos 5 clusters / NLBs internos / RDS / Lambda de auth / VPC Link]
```

- **Entrada**: o API Gateway é o único ponto de entrada público. Ele alcança o NLB interno de cada serviço por um VPC Link, e as rotas do cliente passam antes pelo Lambda Authorizer.
- **Saída**: os nós ficam nas subnets privadas e saem para a internet (ECR, Datadog, Mercado Pago) pelo NAT Gateway.
- **Banco**: cada RDS libera a porta 5432 para o CIDR da VPC. O do core também libera o Security Group da Lambda de autenticação, que lê a tabela `customers`.
- **Entre serviços**: o security group `ofisy-eks-sg`, compartilhado pelos nós dos cinco clusters, libera a porta 8080. Como o NLB é interno e os nós não têm IP público, só o VPC Link e os outros serviços dentro da VPC chegam até ela, como o billing e o notification ao chamar o core.
- **Mensageria**: SNS e SQS ficam fora da VPC. Os nós acessam pelas credenciais da `LabRole`.

---

## Ordem de provisionamento

A ordem completa e o motivo de cada dependência estão no README do [ofisy-platform](https://github.com/15SOAT-FIAP/ofisy-platform). Para o core, o que importa:

1. a rede, o cluster `ms-ofisy-core-cluster`, o RDS `ms-ofisy-core-db` e a mensageria precisam existir antes do deploy, porque o `cd.yml` busca o cluster e o banco pelo nome e a aplicação não sobe sem a fila (`queue-not-found-strategy: fail`);
2. o API Gateway só pode subir depois do deploy, porque descobre o NLB do core pela tag `kubernetes.io/service-name = default/ofisy-service`;
3. o billing e o notification também esperam o core, porque o CD deles descobre o NLB do core para configurar as chamadas síncronas.

Para destruir, o caminho é o inverso: API Gateway, aplicações (`destroy-app.yml`), mensageria e Lambdas, bancos, clusters e, por último, a rede.

---

## Verificar os recursos (AWS CLI)

```bash
# Cluster e nós
aws eks describe-cluster --name ms-ofisy-core-cluster --query "cluster.status"
aws eks update-kubeconfig --region us-east-1 --name ms-ofisy-core-cluster
kubectl get nodes

# Banco
aws rds describe-db-instances --db-instance-identifier ms-ofisy-core-db \
  --query "DBInstances[0].[DBInstanceStatus,Endpoint.Address]"

# Imagens publicadas
aws ecr describe-images --repository-name ms-ofisy-core-ecr --query "imageDetails[].imageTags"

# Fila e DLQ do core
aws sqs get-queue-url --queue-name ofisy-service-order.fifo
aws sqs get-queue-url --queue-name ofisy-service-order-dlq.fifo

# NLB interno do core
aws elbv2 describe-load-balancers \
  --query "LoadBalancers[?Scheme=='internal'].[LoadBalancerName,DNSName]"
```