param (
    [Parameter(Mandatory=$true)]
    [string]$awsAccountId,
    [string]$roleName = "LabRole"
)

$ErrorActionPreference = "Stop"

# 1. Conecta o kubectl ao EKS
Write-Host "☸️ Conectando ao cluster EKS..." -ForegroundColor Cyan
aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster || Write-Host "Cluster EKS não disponível ou já removido." -ForegroundColor Yellow

# 2. Deleta os recursos do Kubernetes (libera ELB e ENIs)
Write-Host "🧹 Deletando serviços do Kubernetes para liberar Load Balancer e placas de rede..." -ForegroundColor Cyan
kubectl delete -f k8s/ --ignore-not-found=true
Write-Host "⏳ Aguardando 30 segundos para a liberação total das placas de rede pela AWS..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 3. Executa a destruição da infraestrutura via Terraform
Write-Host "💥 Destruindo infraestrutura AWS via Terraform..." -ForegroundColor Orange

cd infra/terraform
# Cria arquivos temporários necessários para o comando rodar
"account_id = `"$awsAccountId`"" | Out-File -FilePath terraform.tfvars -Encoding utf8
"role_name  = `"$roleName`"" | Out-File -FilePath terraform.tfvars -Encoding utf8 -Append
"bucket = `"ofisy-tfstate-$awsAccountId`"" | Out-File -FilePath backend.hcl -Encoding utf8

terraform init -backend-config=backend.hcl
terraform destroy -auto-approve

Write-Host "🧹 Removendo arquivos temporários de variáveis..." -ForegroundColor Cyan
Remove-Item -Force terraform.tfvars, backend.hcl, .terraform.lock.hcl -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force .terraform -ErrorAction SilentlyContinue

Write-Host "💥 INFRATESTRUTURA DESTRUÍDA E LIMPA COM SUCESSO!" -ForegroundColor Green
