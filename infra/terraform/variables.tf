variable "account_id" {
  description = "ID da conta AWS"
  type        = string
}

variable "role_name" {
  description = "Nome da IAM Role com acesso ao cluster EKS (no AWS Academy utilize LabRole)"
  type        = string
  default     = "LabRole"
}

variable "db_password" {
  description = "Senha do usuário administrativo do banco de dados PostgreSQL"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_password) >= 8
    error_message = "A senha do banco de dados deve ter no mínimo 8 caracteres."
  }
}
variable "eks_cluster_role" {
  description = "Role utilizada pelo EKS Cluster"
  type        = string
  default     = "c213429a5396203l15812067t1w131560-LabEksClusterRole-Ke9s0hySLUDZ"
}

variable "eks_node_role" {
  description = "Role utilizada pelo EKS Node Group"
  type        = string
  default     = "c213429a5396203l15812067t1w131560185-LabEksNodeRole-vflF20cKYXce"
}
