########################################
# NETWORK — VPC & INTERNET ACCESS
########################################

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${local.project_name}-vpc"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${local.project_name}-igw"
  }
}

########################################
# PUBLIC SUBNETS
########################################

# Subnet pública A — AZ us-east-1a
# O EKS exige ao menos 2 zonas de disponibilidade distintas
resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${local.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name                     = "${local.project_name}-public-subnet-a"
    "kubernetes.io/role/elb" = "1"
  }
}

# Subnet pública B — AZ us-east-1b
resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.4.0/24"
  availability_zone       = "${local.aws_region}b"
  map_public_ip_on_launch = true

  tags = {
    Name                     = "${local.project_name}-public-subnet-b"
    "kubernetes.io/role/elb" = "1"
  }
}

########################################
# PRIVATE SUBNETS
########################################

# Subnet privada A — para o banco de dados e workloads internos
resource "aws_subnet" "private_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${local.aws_region}a"
  map_public_ip_on_launch = false

  tags = {
    Name                              = "${local.project_name}-private-subnet-a"
    "kubernetes.io/role/internal-elb" = "1"
  }
}

# Subnet privada B — segunda AZ obrigatória para o DB Subnet Group do RDS
resource "aws_subnet" "private_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "${local.aws_region}b"
  map_public_ip_on_launch = false

  tags = {
    Name                              = "${local.project_name}-private-subnet-b"
    "kubernetes.io/role/internal-elb" = "1"
  }
}

########################################
# PUBLIC ROUTING
########################################

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${local.project_name}-public-rt"
  }
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

########################################
# DB SUBNET GROUP
########################################

# O RDS exige um subnet group com subnets em pelo menos 2 zonas de disponibilidade distintas
resource "aws_db_subnet_group" "main" {
  name       = "${local.project_name}-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name = "${local.project_name}-db-subnet-group"
  }
}

########################################
# ELASTIC IP - NAT GATEWAY
########################################

resource "aws_eip" "nat" {
  domain = "vpc"

  tags = {
    Name = "${local.project_name}-nat-eip"
  }
}

########################################
# NAT GATEWAY
########################################

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_a.id

  tags = {
    Name = "${local.project_name}-nat"
  }

  depends_on = [
    aws_internet_gateway.main
  ]
}

########################################
# PRIVATE ROUTING
########################################

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  tags = {
    Name = "${local.project_name}-private-rt"
  }
}

resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_b" {
  subnet_id      = aws_subnet.private_b.id
  route_table_id = aws_route_table.private.id
}