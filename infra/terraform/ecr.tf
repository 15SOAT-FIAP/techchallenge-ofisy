########################################
# ECR
########################################

resource "aws_ecr_repository" "main" {
  name                 = "${local.project_name}-ecr"
  image_tag_mutability = "IMMUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${local.project_name}-ecr"
  }
}
