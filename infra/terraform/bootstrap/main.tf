terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.17"
    }
  }
}

########################################
# LOCALS
########################################

locals {
  project_name = "ofisy"
  aws_region   = "us-east-1"
  bucket_name  = "${local.project_name}-tfstate-${var.account_id}"
}

########################################
# PROVIDER
########################################

provider "aws" {
  region = local.aws_region
}
