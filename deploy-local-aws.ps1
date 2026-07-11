param (
    [Parameter(Mandatory=$true)]
    [string]$awsAccountId,
    [string]$roleName = "LabRole"
)

$ErrorActionPreference = "Stop"

# 1. Garante que o bucket do S3 para o Terraform State existe
Write-Host "🔍 Verificando bucket do S3 para o Terraform..." -ForegroundColor Cyan
aws s3api head-bucket --bucket "ofisy-tfstate-$awsAccountId" 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "🏗️ Criando S3 bucket 'ofisy-tfstate-$awsAccountId'..." -ForegroundColor Yellow
    aws s3 mb "s3://ofisy-tfstate-$awsAccountId" --region us-east-1
} else {
    Write-Host "✅ Bucket S3 existente." -ForegroundColor Green
}

# 2. Navega e provisiona a infraestrutura via Terraform
Write-Host "🏗️ Inicializando e aplicando Terraform..." -ForegroundColor Cyan
cd infra/terraform

# Cria arquivos de configuração locais
"account_id = `"$awsAccountId`"" | Out-File -FilePath terraform.tfvars -Encoding utf8
"role_name  = `"$roleName`"" | Out-File -FilePath terraform.tfvars -Encoding utf8 -Append
"bucket = `"ofisy-tfstate-$awsAccountId`"" | Out-File -FilePath backend.hcl -Encoding utf8

terraform init -backend-config=backend.hcl
terraform apply -auto-approve

# Captura endereço do RDS
$rdsHost = terraform output -raw rds_address
Write-Host "✅ Infraestrutura AWS ativa. RDS Host: $rdsHost" -ForegroundColor Green

# 3. Conecta o kubectl ao EKS
Write-Host "☸️ Conectando kubectl ao cluster EKS..." -ForegroundColor Cyan
aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster

# 4. Autentica no ECR
Write-Host "🔐 Autenticando Docker no Amazon ECR..." -ForegroundColor Cyan
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$awsAccountId.dkr.ecr.us-east-1.amazonaws.com"

# 5. Compila a aplicação Java
Write-Host "☕ Compilando projeto Java com Maven..." -ForegroundColor Cyan
cd ../..
.\mvnw.cmd clean package -DskipTests

# 6. Builda e envia a imagem Docker
Write-Host "📤 Construindo e enviando imagem Docker para o ECR..." -ForegroundColor Cyan
docker build -t "$awsAccountId.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest" .
docker push "$awsAccountId.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest"

# 7. Prepara cópia temporária dos manifestos para substituir placeholders sem sujar o git
Write-Host "📝 Configurando e aplicando manifestos do Kubernetes..." -ForegroundColor Cyan
if (Test-Path k8s-build) { Remove-Item -Recurse -Force k8s-build }
New-Item -ItemType Directory -Path k8s-build | Out-Null
Copy-Item -Path k8s/* -Destination k8s-build/

# Substituições de variáveis
(Get-Content k8s-build/secret.yml) `
  -replace '<BASE64_USERNAME>', 'b2Zpc3lfdXNlcg==' `
  -replace '<BASE64_PASSWORD>', 'b2Zpc3lfcGFzcw==' `
  -replace '<BASE64_JWT_SECRET>', 'c3VwZXJfc2VjcmV0X2xvY2FsX2p3dF90b2tlbl9mb3Jfb2Zpc3lfYXBwX2Zhc2VfMg==' `
  | Set-Content k8s-build/secret.yml

(Get-Content k8s-build/configmap.yml) `
  -replace '<RDS_ADDRESS>', $rdsHost `
  | Set-Content k8s-build/configmap.yml

(Get-Content k8s-build/deployment.yml) `
  -replace '<ECR_REPOSITORY_URL>:<IMAGE_TAG>', "$awsAccountId.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest" `
  | Set-Content k8s-build/deployment.yml

# Aplica manifestos configurados
kubectl apply -f k8s-build/secret.yml
kubectl apply -f k8s-build/configmap.yml
kubectl apply -f k8s-build/deployment.yml
kubectl apply -f k8s-build/service.yml
kubectl apply -f k8s-build/hpa.yml

# Remove pasta temporária
Remove-Item -Recurse -Force k8s-build

# Aguarda deploy
Write-Host "⏳ Aguardando inicialização da aplicação no EKS..." -ForegroundColor Cyan
kubectl rollout status deployment/ofisy-deployment --timeout=150s

Write-Host "🚀 DEPLOY AWS CONCLUÍDO COM SUCESSO!" -ForegroundColor Green
