########################################
# OUTPUTS
########################################

# Exibe a URL do repositório Amazon ECR.
output "ecr_repository_url" {
  description = "URL do repositório ECR"
  value       = aws_ecr_repository.main.repository_url
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

# Exibe o endpoint da API do cluster Amazon EKS.
output "eks_cluster_endpoint" {
  description = "Endpoint do API Server do cluster EKS"
  value       = aws_eks_cluster.main.endpoint
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