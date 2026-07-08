########################################
# SECURITY GROUP - EKS
########################################

# Cria um Security Group para controlar o tráfego do cluster EKS.
resource "aws_security_group" "eks" {
  name        = "${local.project_name}-eks-sg"
  description = "Security Group para o cluster EKS"
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${local.project_name}-eks-sg"
  }
}

# Permite a comunicação entre os recursos associados ao mesmo Security Group.
resource "aws_security_group_rule" "eks_self_ingress" {
  type              = "ingress"
  from_port         = 0
  to_port           = 65535
  protocol          = "tcp"
  self              = true
  security_group_id = aws_security_group.eks.id
}

# Permite conexões SSH para acesso administrativo e depuração.
resource "aws_security_group_rule" "eks_ssh_ingress" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks.id
}

# Permite tráfego HTTP de qualquer origem.
resource "aws_security_group_rule" "eks_http_ingress" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks.id
}

# Permite tráfego HTTPS de qualquer origem.
resource "aws_security_group_rule" "eks_https_ingress" {
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks.id
}

# Permite que os recursos do EKS realizem conexões de saída para qualquer destino.
resource "aws_security_group_rule" "eks_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks.id
}

########################################
# SECURITY GROUP - RDS
########################################

# Cria um Security Group para controlar o acesso à instância RDS PostgreSQL.
resource "aws_security_group" "rds" {
  name        = "${local.project_name}-rds-sg"
  description = "Security Group para o banco de dados RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${local.project_name}-rds-sg"
  }
}

# Permite conexões PostgreSQL provenientes do Security Group do EKS.
resource "aws_security_group_rule" "rds_postgres_from_eks" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.eks.id
  security_group_id        = aws_security_group.rds.id
}

# Permite que a instância RDS realize conexões de saída para qualquer destino.
resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.rds.id
}

