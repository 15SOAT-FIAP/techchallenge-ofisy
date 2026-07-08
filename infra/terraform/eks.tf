########################################
# EKS CLUSTER
########################################

# Cria uma IAM Role para o EKS Cluster.
resource "aws_iam_role" "eks_cluster_role" {
  name = "${local.project_name}-eks-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${local.project_name}-eks-cluster-role"
  }
}

# Anexa a política gerenciada necessária para que o EKS gerencie o cluster.
resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster_role.name
}

# Cria o cluster Amazon EKS, configurando VPC, sub-redes, grupo de segurança e logs.
resource "aws_eks_cluster" "main" {
  name            = "${local.project_name}-cluster"
  role_arn        = aws_iam_role.eks_cluster_role.arn

  vpc_config {
    subnet_ids              = [
      aws_subnet.public_a.id,
      aws_subnet.public_b.id,
      aws_subnet.private_a.id,
      aws_subnet.private_b.id
    ]
    security_group_ids      = [aws_security_group.eks.id]
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  enabled_cluster_log_types = ["api", "audit", "authenticator", "controllerManager", "scheduler"]

  tags = {
    Name = "${local.project_name}-cluster"
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
    aws_security_group.eks
  ]
}

########################################
# EKS NODE GROUP
########################################

# Cria uma IAM Role utilizada pelas instâncias EC2 do Node Group.
resource "aws_iam_role" "eks_node_group_role" {
  name = "${local.project_name}-eks-node-group-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${local.project_name}-eks-node-group-role"
  }
}

# Anexa políticas necessárias ao role do node group
resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_group_role.name
}

# Anexa a política necessária para que o plugin de rede (Amazon VPC CNI) funcione nos nós.
resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_group_role.name
}

# Anexa a política que concede permissão para baixar imagens do Amazon ECR.
resource "aws_iam_role_policy_attachment" "eks_container_registry_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_node_group_role.name
}

# Cria um Node Group gerenciado pelo EKS com configuração de escalabilidade e tipo de instância.
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${local.project_name}-node-group"
  node_role_arn   = aws_iam_role.eks_node_group_role.arn
  subnet_ids      = [
    aws_subnet.private_a.id,
    aws_subnet.private_b.id
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
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry_policy,
    aws_eks_cluster.main
  ]
}

########################################
# EKS ACCESS ENTRY (for IAM user/role access)
########################################

# Cria uma entrada de acesso que permite associar uma IAM Role ao cluster EKS.
resource "aws_eks_access_entry" "admin" {
  cluster_name      = aws_eks_cluster.main.name
  principal_arn     = "arn:aws:iam::${var.account_id}:role/${var.role_name}"
  kubernetes_groups = []
  type              = "STANDARD"

  tags = {
    Name = "${local.project_name}-access-entry-admin"
  }

  depends_on = [aws_eks_cluster.main]
}

########################################
# EKS ACCESS POLICY ASSOCIATION
########################################

# Associa uma política de administrador ao principal IAM para conceder acesso ao cluster.
resource "aws_eks_access_policy_association" "admin" {
  cluster_name       = aws_eks_cluster.main.name
  principal_arn      = "arn:aws:iam::${var.account_id}:role/${var.role_name}"
  policy_arn         = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  access_scope {
    type = "cluster"
  }

  depends_on = [aws_eks_access_entry.admin]
}

