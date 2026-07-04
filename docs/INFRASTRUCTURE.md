# Infraestrutura AWS com Terraform — Ofisy

Provisiona toda a infraestrutura AWS da aplicação Ofisy via Terraform, incluindo rede, banco de dados relacional e cluster Kubernetes.

---

## Recursos criados

| Recurso | Tipo Terraform | Descrição |
|---|---|---|
| VPC | `aws_vpc` | Rede isolada `10.0.0.0/16` com suporte a DNS |
| Internet Gateway | `aws_internet_gateway` | Acesso à internet para recursos públicos da VPC |
| Subnet pública A | `aws_subnet` | `10.0.1.0/24` — AZ `us-east-1a` — nós do EKS |
| Subnet pública B | `aws_subnet` | `10.0.4.0/24` — AZ `us-east-1b` — nós do EKS |
| Subnet privada A | `aws_subnet` | `10.0.2.0/24` — AZ `us-east-1a` — banco de dados |
| Subnet privada B | `aws_subnet` | `10.0.3.0/24` — AZ `us-east-1b` — banco de dados |
| Route Table pública | `aws_route_table` | Roteia `0.0.0.0/0` para o Internet Gateway |
| Associações de Route Table | `aws_route_table_association` | Vincula as subnets públicas à route table |
| DB Subnet Group | `aws_db_subnet_group` | Agrupa subnets privadas para uso pelo RDS |
| Security Group EKS | `aws_security_group` | Controla tráfego de entrada/saída do cluster |
| Security Group RDS | `aws_security_group` | Permite acesso ao PostgreSQL apenas pelo EKS |
| RDS PostgreSQL | `aws_db_instance` | Banco de dados relacional gerenciado (`db.t3.micro`) |
| EKS Cluster | `aws_eks_cluster` | Cluster Kubernetes gerenciado na AWS |
| EKS Node Group | `aws_eks_node_group` | Instâncias EC2 `t3.medium` que executam os workloads |
| EKS Access Entry | `aws_eks_access_entry` | Controle de acesso ao cluster via IAM |
| EKS Access Policy | `aws_eks_access_policy_association` | Associa política de administrador ao EKS |

---

## Arquitetura de rede

```
Internet
    │
    ▼
Internet Gateway
    │
    ▼
┌─────────────────────────────────────┐
│  VPC  10.0.0.0/16                   │
│                                     │
│  Subnet pública A   10.0.1.0/24     │ ← EKS Node Group
│  Subnet pública B   10.0.4.0/24     │ ← EKS Node Group
│                                     │
│  Subnet privada A   10.0.2.0/24     │ ← RDS PostgreSQL
│  Subnet privada B   10.0.3.0/24     │ ← RDS PostgreSQL
└─────────────────────────────────────┘
```

As subnets públicas hospedam os nós do EKS e possuem acesso direto à internet via Internet Gateway. As subnets privadas hospedam o banco de dados e só recebem tráfego originado dentro da própria VPC, pelo Security Group do EKS.

---

## Pré-requisitos

- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configurado com credenciais válidas
- Conta AWS com permissões para criar recursos de VPC, EKS e RDS (no AWS Academy, usar sessão ativa do laboratório)

---

## Variáveis

| Variável | Descrição | Padrão |
|---|---|---|
| `account_id` | ID da conta AWS | — |
| `role_name` | IAM Role com acesso ao EKS | `LabRole` |

Copie o arquivo de exemplo e preencha os valores:

```bash
cp terraform.tfvars.example terraform.tfvars
```

---

## Como executar

### Inicializar

```bash
cd infra
terraform init
```

### Validar a configuração

```bash
terraform validate
```

### Visualizar o plano

```bash
terraform plan
```

### Aplicar a infraestrutura

```bash
terraform apply
```

### Destruir os recursos

```bash
terraform destroy
```

---

## Verificar recursos criados (AWS CLI)

```bash
# VPC
aws ec2 describe-vpcs --filters "Name=tag:Name,Values=ofisy-vpc" \
  --query "Vpcs[*].{ID:VpcId,CIDR:CidrBlock,State:State}"

# Subnets
aws ec2 describe-subnets --filters "Name=vpc-id,Values=<VPC_ID>" \
  --query "Subnets[*].{ID:SubnetId,Name:Tags[?Key=='Name']|[0].Value,CIDR:CidrBlock,AZ:AvailabilityZone,Public:MapPublicIpOnLaunch}"

# Internet Gateway
aws ec2 describe-internet-gateways --filters "Name=tag:Name,Values=ofisy-igw" \
  --query "InternetGateways[*].{ID:InternetGatewayId,State:Attachments[0].State}"
```

> Os comandos para verificar o cluster EKS e o RDS serão adicionados após a implementação dos serviços da aplicação.
