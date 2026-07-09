########################################
# RDS POSTGRESQL DATABASE
########################################

# Cria uma instância Amazon RDS utilizando o banco de dados PostgreSQL.
resource "aws_db_instance" "main" {
  allocated_storage      = 20
  storage_type           = "gp2"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = "db.t3.micro"
  username               = "admin"
  password               = var.db_password
  parameter_group_name   = aws_db_parameter_group.main.name
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  # Define o nome do banco de dados.
  db_name = "ofisydb"

  # Configura a política de backup e a janela de manutenção.
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  # Configura o snapshot final, a disponibilidade e a segurança da instância.
  skip_final_snapshot       = true

  multi_az            = false
  publicly_accessible = false
  storage_encrypted   = true

  tags = {
    Name = "${local.project_name}-postgres-db"
  }

  depends_on = [
    aws_security_group.rds
  ]
}

# Cria um Parameter Group para personalizar as configurações do PostgreSQL.
resource "aws_db_parameter_group" "main" {
  name   = "${local.project_name}-postgres-params"
  family = "postgres15"

  # Define o número máximo de conexões simultâneas com o banco.
  parameter {
    name  = "max_connections"
    value = "100"
  }

  # Configura a quantidade de memória destinada ao cache de dados do PostgreSQL.
  parameter {
    name  = "shared_buffers"
    value = "{DBInstanceClassMemory/32}"
  }

  tags = {
    Name = "${local.project_name}-postgres-params"
  }
}