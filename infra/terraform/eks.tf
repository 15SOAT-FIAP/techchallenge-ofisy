########################################
# EKS CLUSTER
########################################

locals {
  eks_cluster_role_arn = "arn:aws:iam::${var.account_id}:role/${var.eks_cluster_role}"
  eks_node_role_arn = "arn:aws:iam::${var.account_id}:role/${var.eks_node_role}"
  access_role_arn = "arn:aws:iam::${var.account_id}:role/${var.role_name}"
}

# Cria o cluster Amazon EKS, configurando VPC, sub-redes, grupo de segurança e logs.
resource "aws_eks_cluster" "main" {
  name     = "${local.project_name}-cluster"
  role_arn = local.eks_cluster_role_arn

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }

  vpc_config {
    subnet_ids = [
      aws_subnet.public_a.id,
      aws_subnet.public_b.id,
      aws_subnet.private_a.id,
      aws_subnet.private_b.id
    ]

    security_group_ids      = [aws_security_group.eks.id]
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  # Habilita os logs do Control Plane do Kubernetes.
  enabled_cluster_log_types = [
    "api",
    "audit",
    "authenticator",
    "controllerManager",
    "scheduler"
  ]

  tags = {
    Name = "${local.project_name}-cluster"
  }
}

########################################
# EKS NODE GROUP
########################################

# Cria um Node Group gerenciado pelo EKS com configuração de escalabilidade e tipo de instância.
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${local.project_name}-node-group"
  node_role_arn = local.eks_node_role_arn

  subnet_ids = [
    aws_subnet.public_a.id,
    aws_subnet.public_b.id
  ]

  version = aws_eks_cluster.main.version

  scaling_config {
    desired_size = 2
    max_size     = 4
    min_size     = 1
  }

  instance_types = ["t3.medium"]

  tags = {
    Name = "${local.project_name}-node-group"
  }

  depends_on = [
    aws_eks_cluster.main
  ]
}

########################################
# EKS ACCESS ENTRY
########################################

# Cria uma entrada de acesso que permite associar uma IAM Role ao cluster EKS.
resource "aws_eks_access_entry" "admin" {
  cluster_name      = aws_eks_cluster.main.name
  principal_arn     = local.access_role_arn
  kubernetes_groups = []
  type              = "STANDARD"

  tags = {
    Name = "${local.project_name}-access-entry-admin"
  }

  depends_on = [
    aws_eks_cluster.main
  ]
}

########################################
# EKS ACCESS POLICY ASSOCIATION
########################################

# Associa uma política de administrador ao principal IAM para conceder acesso ao cluster.
resource "aws_eks_access_policy_association" "admin" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = local.access_role_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }

  depends_on = [
    aws_eks_access_entry.admin
  ]
}