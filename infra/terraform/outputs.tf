########################################
# OUTPUTS
########################################

# Exibe o ID da VPC criada.
output "vpc_id" {
  description = "ID da VPC criada"
  value       = aws_vpc.main.id
}

# Exibe o bloco CIDR configurado para a VPC.
output "vpc_cidr" {
  description = "CIDR block da VPC"
  value       = aws_vpc.main.cidr_block
}

# Exibe o ID da subnet pública A.
output "public_subnet_a_id" {
  description = "ID da subnet pública A"
  value       = aws_subnet.public_a.id
}

# Exibe o ID da subnet pública B.
output "public_subnet_b_id" {
  description = "ID da subnet pública B"
  value       = aws_subnet.public_b.id
}

# Exibe o ID da subnet privada A.
output "private_subnet_a_id" {
  description = "ID da subnet privada A"
  value       = aws_subnet.private_a.id
}

# Exibe o ID da subnet privada B.
output "private_subnet_b_id" {
  description = "ID da subnet privada B"
  value       = aws_subnet.private_b.id
}

# Exibe o ID do Security Group utilizado pelo cluster EKS.
output "eks_security_group_id" {
  description = "ID do Security Group do EKS"
  value       = aws_security_group.eks.id
}

# Exibe o ID do Security Group utilizado pelo banco RDS.
output "rds_security_group_id" {
  description = "ID do Security Group do RDS"
  value       = aws_security_group.rds.id
}

# Exibe a URL do repositório Amazon ECR.
output "ecr_repository_url" {
  description = "URL do repositório ECR"
  value       = aws_ecr_repository.main.repository_url
}

# Exibe o nome do repositório Amazon ECR.
output "ecr_repository_name" {
  description = "Nome do repositório ECR"
  value       = aws_ecr_repository.main.name
}

# Exibe o endpoint de conexão da instância RDS.
output "rds_endpoint" {
  description = "Endpoint de conexão do banco de dados RDS"
  value       = aws_db_instance.main.endpoint
}

# Exibe o endereço (hostname) da instância RDS.
output "rds_address" {
  description = "Endereço do banco de dados RDS (hostname)"
  value       = aws_db_instance.main.address
}

# Exibe a porta de conexão da instância RDS.
output "rds_port" {
  description = "Porta de conexão do banco de dados RDS"
  value       = aws_db_instance.main.port
}

# Exibe o nome do banco de dados.
output "rds_database_name" {
  description = "Nome do banco de dados"
  value       = aws_db_instance.main.db_name
}

# Exibe o usuário administrador da instância RDS.
output "rds_username" {
  description = "Usuário administrativo do banco de dados"
  value       = aws_db_instance.main.username
  sensitive   = true
}

# Exibe o nome do cluster Amazon EKS.
output "eks_cluster_name" {
  description = "Nome do cluster EKS"
  value       = aws_eks_cluster.main.name
}

# Exibe o ARN do cluster Amazon EKS.
output "eks_cluster_arn" {
  description = "ARN do cluster EKS"
  value       = aws_eks_cluster.main.arn
}

# Exibe o endpoint da API do cluster Amazon EKS.
output "eks_cluster_endpoint" {
  description = "Endpoint do API Server do cluster EKS"
  value       = aws_eks_cluster.main.endpoint
}

# Exibe a versão do Kubernetes utilizada pelo cluster.
output "eks_cluster_version" {
  description = "Versão do Kubernetes no cluster EKS"
  value       = aws_eks_cluster.main.version
}

# Exibe o certificado da Autoridade Certificadora (CA) do cluster EKS.
output "eks_cluster_certificate_authority" {
  description = "Certificado de autoridade do cluster EKS"
  value       = aws_eks_cluster.main.certificate_authority[0].data
  sensitive   = true
}

# Exibe o ID do Security Group do cluster EKS.
output "eks_cluster_security_group_id" {
  description = "ID do Security Group do cluster EKS"
  value       = aws_eks_cluster.main.vpc_config[0].cluster_security_group_id
}

# Exibe o nome do Node Group do EKS.
output "eks_node_group_name" {
  description = "Nome do Node Group"
  value       = aws_eks_node_group.main.node_group_name
}

# Exibe o ARN do Node Group do EKS.
output "eks_node_group_arn" {
  description = "ARN do Node Group"
  value       = aws_eks_node_group.main.arn
}

# Exibe o status do Node Group do EKS.
output "eks_node_group_status" {
  description = "Status do Node Group"
  value       = aws_eks_node_group.main.status
}

# Exibe o nome configurado para o projeto.
output "project_name" {
  description = "Nome do projeto"
  value       = local.project_name
}

# Exibe a região da AWS utilizada na infraestrutura.
output "aws_region" {
  description = "Região AWS utilizada"
  value       = local.aws_region
}

# Exibe o ID da conta AWS utilizada.
output "account_id" {
  description = "ID da conta AWS"
  value       = var.account_id
}