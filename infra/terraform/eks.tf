resource "aws_eks_cluster" "ofisy" {
  name     = "${local.project_name}-eks-cluster"
  role_arn = "arn:aws:iam::${var.account_id}:role/${var.role_name}"

  vpc_config {
    subnet_ids = [
      aws_subnet.public_a.id,
      aws_subnet.public_b.id,
      aws_subnet.private_a.id,
      aws_subnet.private_b.id
    ]
  }
}

resource "aws_eks_node_group" "nodes" {
  cluster_name    = aws_eks_cluster.ofisy.name
  node_group_name = "${local.project_name}-node-group"
  node_role_arn   = "arn:aws:iam::${var.account_id}:role/${var.role_name}"
  subnet_ids      = [aws_subnet.public_a.id, aws_subnet.public_b.id]

  scaling_config {
    desired_size = 2
    max_size     = 5
    min_size     = 1
  }

  instance_types = ["t3.medium"]
}
