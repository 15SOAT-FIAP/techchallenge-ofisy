########################################
# S3 — REMOTE STATE
########################################

resource "aws_s3_bucket" "tfstate" {
  bucket        = local.bucket_name
  force_destroy = false

  tags = {
    Name = local.bucket_name
  }
}

# Versionamento permite recuperar versões anteriores do tfstate
resource "aws_s3_bucket_versioning" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Bloqueia acesso público ao bucket de estado
resource "aws_s3_bucket_public_access_block" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

output "bucket_name" {
  description = "Nome do bucket S3 para uso no backend.hcl"
  value       = aws_s3_bucket.tfstate.bucket
}
