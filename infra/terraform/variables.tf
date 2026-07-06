variable "account_id" {
  description = "ID da conta AWS"
  type        = string
}

variable "role_name" {
  description = "Nome da IAM Role com acesso ao cluster EKS (no AWS Academy utilize LabRole)"
  type        = string
  default     = "LabRole"
}
