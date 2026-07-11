terraform {
  backend "s3" {
    # Valores fornecidos via: terraform init -backend-config=backend.hcl
    key    = "terraform.tfstate"
    region = "us-east-1"
  }
}
