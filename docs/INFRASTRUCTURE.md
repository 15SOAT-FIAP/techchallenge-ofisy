# Infraestrutura AWS com Terraform — Ofisy

Provisiona toda a infraestrutura AWS da aplicação Ofisy via Terraform, incluindo rede, banco de dados relacional e cluster Kubernetes.

Este documento aborda:

- Quais recursos AWS são criados e suas responsabilidades
- Arquitetura de rede e isolamento entre camadas pública e privada
- Pré-requisitos para execução
- Variáveis de configuração necessárias
- Passo a passo para inicializar, validar e aplicar a infraestrutura
- Comandos AWS CLI para verificar os recursos criados

---

## Recursos criados

| Recurso | Tipo Terraform | Descrição |
|---|---|---|
| VPC | `aws_vpc` | Rede isolada `10.0.0.0/16` com suporte a DNS |
| Internet Gateway | `aws_internet_gateway` | Acesso à internet para recursos públicos da VPC |
| Subnet pública A | `aws_subnet` | `10.0.1.0/24` — AZ `us-east-1a` — Load balancer e EKS |
| Subnet pública B | `aws_subnet` | `10.0.4.0/24` — AZ `us-east-1b` — Load balancer e EKS |
| Subnet privada A | `aws_subnet` | `10.0.2.0/24` — AZ `us-east-1a` — RDS e Pods internos |
| Subnet privada B | `aws_subnet` | `10.0.3.0/24` — AZ `us-east-1b` — RDS e Pods internos |
| Route Table pública | `aws_route_table` | Roteia `0.0.0.0/0` para o Internet Gateway |
| Associações de Route Table | `aws_route_table_association` | Vincula as subnets públicas à route table |
| DB Subnet Group | `aws_db_subnet_group` | Agrupa subnets privadas para uso pelo RDS |
| ECR | `aws_ecr_repository` | Repositório de imagens Docker da aplicação |
| Security Group EKS | `aws_security_group` | Firewall do EKS — abre SSH(22), HTTP(80), HTTPS(443) e tráfego interno |
| Security Group RDS | `aws_security_group` | Firewall do RDS — permite PostgreSQL(5432) apenas de EKS |
| RDS PostgreSQL | `aws_db_instance` | PostgreSQL 15.4 gerenciado (`db.t3.micro`, 20GB, backup 7 dias, criptografia habilitada) |
| RDS Parameter Group | `aws_db_parameter_group` | Configurações otimizadas para o PostgreSQL |
| EKS Cluster | `aws_eks_cluster` | Kubernetes v1.29 gerenciado — control plane, logging (5 tipos) |
| EKS Cluster IAM Role | `aws_iam_role` | Permissões para o control plane do EKS |
| EKS Node Group | `aws_eks_node_group` | 2 instâncias `t3.medium` (min:1, max:4) que executam os workloads |
| EKS Node Group IAM Role | `aws_iam_role` | Permissões para nodes (worker policy, CNI, ECR read) |
| EKS Access Entry | `aws_eks_access_entry` | Controle de acesso ao cluster via IAM Role |
| EKS Access Policy Association | `aws_eks_access_policy_association` | Associa política de administrador para acesso ao cluster |

---

## Arquitetura de rede

```
Internet
    │
    ▼ (HTTP/HTTPS portas 80, 443)
Internet Gateway (ofisy-igw)
    │
    ▼ (0.0.0.0/0)
┌────────────────────────────────────────────────────────┐
│  VPC  10.0.0.0/16                                      │
│                                                        │
│  Public Subnets (Expostas à internet)                 │
│  ┌──────────────────┐      ┌──────────────────┐      │
│  │ Subnet A         │      │ Subnet B         │      │
│  │ 10.0.1.0/24      │      │ 10.0.4.0/24      │      │
│  │ us-east-1a       │      │ us-east-1b       │      │
│  │ ┌──────────────┐ │      │ ┌──────────────┐ │      │
│  │ │ EKS Node 1   │ │      │ │ EKS Node 2   │ │      │
│  │ │ t3.medium    │ │      │ │ t3.medium    │ │      │
│  │ │ (Pods)       │ │      │ │ (Pods)       │ │      │
│  │ └──────────────┘ │      │ └──────────────┘ │      │
│  └──────────────────┘      └──────────────────┘      │
│                                                        │
│  Security Group EKS                                   │
│  ├─ SSH (22), HTTP (80), HTTPS (443)                 │
│  └─ Tráfego interno entre nodes                      │
│                                                        │
│  Private Subnets (Isoladas da internet)              │
│  ┌──────────────────────────┐  ┌────────────────┐   │
│  │ Subnet A                 │  │ Subnet B       │   │
│  │ 10.0.2.0/24              │  │ 10.0.3.0/24    │   │
│  │ us-east-1a               │  │ us-east-1b     │   │
│  │ ┌──────────────────────┐ │  │                │   │
│  │ │ RDS PostgreSQL 15.4  │ │  │ (Standby)      │   │
│  │ │ ofisy-postgres-db    │ │  │                │   │
│  │ │ db.t3.micro          │ │  │                │   │
│  │ │ 20GB storage         │ │  │                │   │
│  │ │ Criptografia: SIM    │ │  │                │   │
│  │ │ Backup: 7 dias       │ │  │                │   │
│  │ └──────────────────────┘ │  │                │   │
│  └──────────────────────────┘  └────────────────┘   │
│                                                        │
│  Security Group RDS                                   │
│  └─ PostgreSQL (5432) apenas de EKS                  │
│                                                        │
└────────────────────────────────────────────────────────┘

EKS Control Plane (AWS Managed)
├─ Kubernetes v1.29
├─ API Server: https://[ID].eks.us-east-1.amazonaws.com
├─ Logging: API, Audit, Authenticator, ControllerManager, Scheduler
└─ Endpoints: Público + Privado
```

#### Fluxo de Dados

1. **Requisição externa** → Internet Gateway → Public Subnet (Load Balancer)
2. **Load Balancer** → EKS Nodes (via Security Group EKS)
3. **Pods** → Query SQL → RDS (via Security Group RDS, porta 5432)
4. **RDS** → Retorna dados → Pods
5. **Pods** → Response → Load Balancer → Cliente

#### Isolamento de Segurança

- **RDS em subnets privadas** — sem acesso direto da internet
- **RDS só aceita conexões do Security Group EKS** — isolamento em nível de firewall
- **EKS nodes em subnets privadas** — acessíveis apenas via Load Balancer público
- **Backups automáticos** — diários com retenção de 7 dias
- **Criptografia** — dados armazenados criptografados

---

## Pré-requisitos

- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configurado com credenciais válidas
- Conta AWS com permissões para criar recursos de VPC, EKS e RDS (no AWS Academy, usar sessão ativa do laboratório)

---

## Variáveis

| Variável | Descrição | Padrão | Obrigatório |
|---|---|---|---|
| `account_id` | ID da conta AWS (12 dígitos) | — | ✅ SIM |
| `role_name` | IAM Role com acesso ao EKS | `LabRole` | No AWS Academy |
| `db_password` | Senha do usuário admin do banco de dados | — | ✅ SIM |

**Configuração:**

```bash
# Opção A: Via arquivo (menos seguro)
cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
# Editar terraform.tfvars com seus valores

# Opção B: Via variáveis de ambiente (RECOMENDADO)
export TF_VAR_account_id="123456789012"
export TF_VAR_role_name="LabRole"
export TF_VAR_db_password="SenhaSegura123"
```

---

## Como executar

### 1. Bootstrap — criar o bucket S3 para o estado remoto

Execute apenas uma vez no início. O bucket não é destruído com `terraform destroy`.

```bash
cd infra/terraform/bootstrap
cp terraform.tfvars.example terraform.tfvars
# Editar: preencher account_id no terraform.tfvars
terraform init
terraform apply
```

Copie o valor de `bucket_name` retornado no output.

### 2. Configurar o backend remoto

```bash
cd ../  # volta para infra/terraform
cp backend.hcl.example backend.hcl
# Editar: preencher o bucket com o valor retornado no passo anterior
```

### 3. Inicializar Terraform

```bash
terraform init -backend-config=backend.hcl
```

Se você já fez `terraform apply` com estado local:
```bash
terraform init -backend-config=backend.hcl -migrate-state
```

### 4. Configurar as variáveis

Escolha uma das opções:

**Opção A: Arquivo (menos seguro)**
```bash
cp terraform.tfvars.example terraform.tfvars
# Editar com seus valores
```

**Opção B: Variáveis de ambiente (RECOMENDADO)**
```bash
export TF_VAR_account_id="123456789012"
export TF_VAR_role_name="LabRole"
export TF_VAR_db_password="SenhaSegura123"
```

### 5. Validar a configuração

```bash
terraform validate
```

### 6. Visualizar o plano

```bash
terraform plan
```

Revise os recursos que serão criados.

### 7. Aplicar a infraestrutura

```bash
terraform apply
```

**Tempo estimado:**
- RDS: ~5-7 minutos
- EKS Cluster: ~7-10 minutos
- EKS Node Group: ~3-5 minutos
- **Total: 15-20 minutos**

### 8. Obter outputs

```bash
terraform output
```

Copie os valores importantes:
- `eks_cluster_name` — para uso com `kubectl`
- `rds_endpoint` — para conectar no banco
- `ecr_repository_url` — para push de imagens Docker

### 9. Destruir os recursos

```bash
terraform destroy
```

O bucket S3 do bootstrap **não é destruído**. Para removê-lo:
```bash
cd bootstrap
terraform destroy
```

---

## Verificar recursos criados (AWS CLI)

### Visualizar todos os outputs do Terraform

```bash
terraform output
# ou em formato JSON:
terraform output -json
```

### Verificar infraestrutura de rede (Bootstrap/VPC/Subnets)

```bash
# S3 (estado remoto)
aws s3 ls | grep ofisy-tfstate

# VPC
aws ec2 describe-vpcs --filters "Name=tag:Name,Values=ofisy-vpc" \
  --query "Vpcs[*].{ID:VpcId,CIDR:CidrBlock,State:State}"

# Subnets (substitua SEU_VPC_ID pelo ID da VPC)
SEU_VPC_ID=$(aws ec2 describe-vpcs --filters "Name=tag:Name,Values=ofisy-vpc" \
  --query "Vpcs[0].VpcId" --output text)
  
aws ec2 describe-subnets --filters "Name=vpc-id,Values=$SEU_VPC_ID" \
  --query "Subnets[*].{ID:SubnetId,Name:Tags[?Key=='Name']|[0].Value,CIDR:CidrBlock,AZ:AvailabilityZone,Public:MapPublicIpOnLaunch}"

# Internet Gateway
aws ec2 describe-internet-gateways --filters "Name=tag:Name,Values=ofisy-igw" \
  --query "InternetGateways[*].{ID:InternetGatewayId,State:Attachments[0].State,VPC:Attachments[0].VpcId}"

# DB Subnet Group
aws rds describe-db-subnet-groups --db-subnet-group-name ofisy-db-subnet-group \
  --query "DBSubnetGroups[*].{Name:DBSubnetGroupName,Subnets:DBSubnetGroupDescription,VPC:VpcId}"
```

### Verificar Security Groups

```bash
# Security Group EKS
aws ec2 describe-security-groups --filters "Name=tag:Name,Values=ofisy-eks-sg" \
  --query "SecurityGroups[*].{ID:GroupId,Name:GroupName,VPC:VpcId,InboundRules:IpPermissions[*].{FromPort:FromPort,ToPort:ToPort,Protocol:IpProtocol}}"

# Security Group RDS
aws ec2 describe-security-groups --filters "Name=tag:Name,Values=ofisy-rds-sg" \
  --query "SecurityGroups[*].{ID:GroupId,Name:GroupName,VPC:VpcId,InboundRules:IpPermissions[*].{FromPort:FromPort,ToPort:ToPort,Protocol:IpProtocol}}"
```

### Verificar ECR

```bash
aws ecr describe-repositories --query "repositories[*].{Name:repositoryName,URI:repositoryUri,ScanOnPush:imageScanningConfiguration.scanOnPush}"
```

### Verificar RDS PostgreSQL

```bash
# Informações gerais do banco
aws rds describe-db-instances --db-instance-identifier ofisy-postgres-db \
  --query "DBInstances[*].{Endpoint:Endpoint.Address,Port:Endpoint.Port,Engine:Engine,Version:EngineVersion,Class:DBInstanceClass,Storage:AllocatedStorageMB,Status:DBInstanceStatus,Backup:BackupRetentionPeriod,Encrypted:StorageEncrypted}"

# Testar conectividade com o banco (se psql estiver instalado)
ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier ofisy-postgres-db \
  --query "DBInstances[0].Endpoint.Address" --output text)
echo "Conectando em: $ENDPOINT"
psql -h $ENDPOINT -U admin -d ofisydb -c "SELECT version();"
```

### Verificar EKS Cluster

```bash
# Informações do cluster
aws eks describe-cluster --name ofisy-cluster \
  --query "cluster.{Name:name,Version:version,Status:status,Endpoint:endpoint,Logging:logging.clusterLogging[*].types}"

# Atualizar kubeconfig para usar kubectl
aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster

# Verificar acesso ao cluster
kubectl cluster-info
kubectl get cluster-info

# Ver os nodes
kubectl get nodes -o wide

# Ver pods rodando
kubectl get pods -A
```

### Verificar EKS Node Group

```bash
# Informações do node group
aws eks describe-nodegroup --cluster-name ofisy-cluster --nodegroup-name ofisy-node-group \
  --query "nodegroup.{Name:nodegroupName,Status:status,InstanceTypes:instanceTypes,DesiredSize:scalingConfig.desiredSize,MinSize:scalingConfig.minSize,MaxSize:scalingConfig.maxSize}"

# Detailed node information via kubectl
kubectl describe nodes
```

### Verificar IAM Roles

```bash
# EKS Cluster Role
aws iam get-role --role-name ofisy-eks-cluster-role \
  --query "Role.{Name:RoleName,Arn:Arn,Created:CreateDate}"

# EKS Node Group Role
aws iam get-role --role-name ofisy-eks-node-group-role \
  --query "Role.{Name:RoleName,Arn:Arn,Created:CreateDate}"

# Policies anexadas ao Cluster Role
aws iam list-attached-role-policies --role-name ofisy-eks-cluster-role

# Policies anexadas ao Node Group Role
aws iam list-attached-role-policies --role-name ofisy-eks-node-group-role
```

### Verificação rápida — Tudo em um comando

```bash
echo "=== TERRAFORM OUTPUTS ===" && terraform output && \
echo -e "\n=== EKS CLUSTER ===" && aws eks describe-cluster --name ofisy-cluster --query "cluster.{Name:name,Status:status,Version:version}" && \
echo -e "\n=== EKS NODES ===" && kubectl get nodes && \
echo -e "\n=== RDS DATABASE ===" && aws rds describe-db-instances --db-instance-identifier ofisy-postgres-db --query "DBInstances[0].{Endpoint:Endpoint.Address,Status:DBInstanceStatus,Engine:Engine}" && \
echo -e "\n=== SECURITY GROUPS ===" && aws ec2 describe-security-groups --filters "Name=tag:Name,Values=ofisy-*" --query "SecurityGroups[*].{Name:GroupName,ID:GroupId}"
```
