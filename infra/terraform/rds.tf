resource "aws_security_group" "db" {
  name        = "${local.project_name}-db-sg"
  description = "Acesso ao PostgreSQL RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.main.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${local.project_name}-db-sg"
  }
}

resource "aws_db_instance" "postgres" {
  allocated_storage      = 20
  max_allocated_storage  = 100
  db_name                = "ofisydb"
  engine                 = "postgres"
  engine_version         = "16"
  instance_class         = "db.t4g.micro"
  username               = "ofisy_user"
  password               = "ofisy_pass"
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]
  skip_final_snapshot    = true
  publicly_accessible    = false

  tags = {
    Name = "${local.project_name}-rds-postgres"
  }
}

output "rds_endpoint" {
  value       = aws_db_instance.postgres.address
  description = "Endpoint do banco de dados RDS"
}
