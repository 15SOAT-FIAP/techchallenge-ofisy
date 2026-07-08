# 🚀 Guia de Execução e Deploy — Local & AWS Cloud (Fase 2)

Este guia descreve os procedimentos detalhados para executar, testar e fazer o deploy da aplicação **Ofisy** nos ambientes de desenvolvimento local (Docker Compose & Minikube) e em produção na nuvem da AWS (via GitHub Actions ou linha de comando).

---

## 💻 1. Execução Local Rápida (Docker Compose)

Cenário para testes rápidos de desenvolvimento local com aplicação e banco rodando em containers integrados:

1.  Acesse a pasta raiz do projeto.
2.  Copie o arquivo de exemplo de variáveis de ambiente:
    ```bash
    cp env.example .env
    ```
3.  Suba os containers:
    ```bash
    docker compose up --build
    ```
4.  O Swagger estará acessível em: `http://localhost:8080/swagger-ui/index.html`

---

## ☸️ 2. Deploy Local em Kubernetes (Minikube)

Cenário de simulação de orquestração local com Kubernetes para validar os manifestos cloud-ready:

1.  Inicie o cluster local com o driver Docker:
    ```powershell
    minikube start --driver=docker
    ```
2.  Aponte o terminal para o Docker interno do Minikube:
    ```powershell
    minikube docker-env | Invoke-Expression
    ```
3.  Compile o pacote Spring Boot (gerando o `.jar`):
    ```powershell
    .\mvnw.cmd clean package -DskipTests
    ```
4.  Gere a imagem localmente:
    ```powershell
    docker build -t ofisy-app:latest .
    ```
5.  Aplique os manifestos do banco local e do aplicativo:
    ```powershell
    kubectl apply -f infra/k8s/db/
    kubectl apply -f infra/k8s/app/
    ```
6.  Abra o túnel de rede local (em um terminal de Administrador separado):
    ```powershell
    minikube tunnel
    ```
7.  Acesse o IP do serviço (`kubectl get svc ofisy-service`) na porta `8080`.

---

## ☁️ 3. Deploy na AWS via GitHub Actions (Automático - Recomendado)

Este método automatiza o provisionamento do Terraform, o build do código e o deploy no cluster EKS usando a esteira de CI/CD integrada no repositório.

### Passo 1: Obter as Credenciais Temporárias da AWS Academy
1.  Acesse seu laboratório da **AWS Academy** e clique em **Start Lab**.
2.  Assim que o laboratório iniciar, clique no botão **AWS Details**.
3.  Ao lado de *AWS CLI Credentials*, clique em **Show** e copie os valores das chaves:
    *   `aws_access_key_id`
    *   `aws_secret_access_key`
    *   `aws_session_token`

### Passo 2: Executar o Workflow no GitHub
1.  Acesse o repositório do projeto no GitHub e clique na aba **Actions**.
2.  No menu lateral esquerdo, selecione a esteira **Validacao de Deploy e Entrega CD**.
3.  Clique no botão **Run workflow** à direita.
4.  Preencha as opções:
    *   Marque **`deploy_to_aws`** como `true`.
    *   Cole as chaves correspondentes copias da AWS Academy nos campos apropriados de input.
5.  Clique em **Run workflow**. 
    *   *A esteira irá configurar o Terraform, aplicar o RDS e o EKS, buildar a aplicação, enviá-la ao ECR e realizar o deploy final no Kubernetes da AWS.*

---

## ☁️ 4. Deploy na AWS via CLI (Manual)

Se você preferir executar o provisionamento e o deploy manualmente a partir da sua máquina:

### Passo 1: Autenticar seu terminal local
Cole as credenciais do AWS Academy formatadas para o PowerShell no seu terminal:
```powershell
$env:AWS_ACCESS_KEY_ID="SUA_KEY"
$env:AWS_SECRET_ACCESS_KEY="SUA_SECRET"
$env:AWS_SESSION_TOKEN="SEU_TOKEN"
```

### Passo 2: Provisionar a infraestrutura (Terraform)
1.  Navegue até a pasta de bootstrap para criar o bucket de estado remoto (S3):
    ```powershell
    cd infra/terraform/bootstrap
    echo 'account_id = "058943964484"' > terraform.tfvars
    terraform init
    terraform apply -auto-approve
    ```
2.  Retorne à pasta principal do Terraform para subir o EKS e o RDS:
    ```powershell
    cd ..
    echo 'bucket = "ofisy-tfstate-058943964484"' > backend.hcl
    echo 'account_id = "058943964484"' > terraform.tfvars
    echo 'role_name  = "LabRole"' >> terraform.tfvars
    
    terraform init "-backend-config=backend.hcl"
    terraform apply -auto-approve
    ```

### Passo 3: Enviar a imagem e aplicar os manifestos
1.  Conecte seu `kubectl` ao cluster EKS criado:
    ```powershell
    aws eks update-kubeconfig --region us-east-1 --name ofisy-eks-cluster
    ```
2.  Insira o endpoint do banco RDS (gerado pelo Terraform) no arquivo `infra/k8s/app/configmap.yml` no campo `POSTGRES_HOST`.
3.  Autentique o Docker no ECR, compile a aplicação e faça o push:
    ```powershell
    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 058943964484.dkr.ecr.us-east-1.amazonaws.com
    .\mvnw.cmd clean package -DskipTests
    docker build -t ofisy-app:latest .
    docker tag ofisy-app:latest 058943964484.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest
    docker push 058943964484.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest
    ```
4.  Aplique os manifestos no cluster:
    ```powershell
    kubectl apply -f infra/k8s/db/secret.yml
    kubectl apply -f infra/k8s/db/configmap.yml
    kubectl apply -f infra/k8s/app/
    ```

---

## ⚠️ 5. Limpeza e Destruição (Economia de Créditos)

**Importante:** Os recursos na AWS gerados por esse deploy consomem cerca de $4.00 de créditos virtuais do seu laboratório por dia. Destrua-os sempre que terminar as apresentações ou avaliações.

### Método A: Via GitHub Actions (Recomendado)
1.  Acesse a aba **Actions** > **Validacao de Deploy e Entrega CD**.
2.  Clique em **Run workflow**.
3.  Marque **`destroy_aws`** como `true` e cole as credenciais de autenticação da AWS Academy.
4.  Execute o workflow para remover os serviços e a infraestrutura de forma limpa.

### Método B: Via CLI Local
No terminal do PowerShell autenticado, entre na pasta do Terraform e execute a destruição:
```powershell
cd infra/terraform
terraform destroy -auto-approve
```
