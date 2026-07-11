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

Cenário de simulação de orquestração local com Kubernetes para validar os manifestos.

> [!NOTE]
> Como os manifestos do banco de dados local foram removidos da pasta do Kubernetes (pois na nuvem a aplicação se conecta diretamente no AWS RDS), para rodar a aplicação localmente no Minikube você precisará subir o banco de dados separadamente (por exemplo, usando o Docker Compose local) e atualizar o `POSTGRES_HOST` no `k8s/configmap.yml` para apontar para o IP/host do banco acessível pelo Minikube.

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
5.  Configure o secret local com credenciais mockadas e o configmap apontando para o seu banco:
    ```powershell
    kubectl create secret generic ofisy-secret `
      --from-literal=POSTGRES_USER=ofisy_user `
      --from-literal=POSTGRES_PASSWORD=ofisy_pass `
      --from-literal=JWT_SECRET=super_secret_local_jwt_token_for_ofisy_app_fase_2
    ```
6.  Aplique os manifestos do aplicativo:
    ```powershell
    kubectl apply -f k8s/configmap.yml
    kubectl apply -f k8s/deployment.yml
    kubectl apply -f k8s/service.yml
    kubectl apply -f k8s/hpa.yml
    ```
7.  Abra o túnel de rede local (em um terminal de Administrador separado):
    ```powershell
    minikube tunnel
    ```
8.  Acesse o IP do serviço (`kubectl get svc ofisy-service`) na porta `8080`.

---

## ☁️ 3. Deploy na AWS via GitHub Actions (Automático - Recomendado)

Este método automatiza o planejamento do Terraform (`terraform plan`), o provisionamento (`terraform apply`), o build do código e o deploy no cluster EKS da AWS. Ele é acionado **automaticamente** ao realizar o push ou merge de qualquer alteração na branch `master`.

### Passo 1: Configurar o Environment e Secrets no GitHub
Como a esteira roda de forma 100% autônoma e segura, você deve criar um ambiente (**Environment**) no GitHub chamado **`AWS_ACADEMY`** e salvar as credenciais lá como **Environment Secrets**:

1. No GitHub, acesse seu repositório e vá em **Settings** > **Environments** > **New environment**.
2. Nomeie o ambiente como **`AWS_ACADEMY`** e clique em **Configure environment**.
3. Na seção **Environment secrets**, clique em **Add secret** para adicionar as seguintes chaves:
    *   `AWS_ACCESS_KEY_ID`: A sua *AWS Access Key ID* (obtida no painel *AWS Details* do Learner Lab).
    *   `AWS_SECRET_ACCESS_KEY`: A sua *AWS Secret Access Key*.
    *   `AWS_SESSION_TOKEN`: O seu *AWS Session Token*.
    *   `AWS_ACCOUNT_ID`: O ID numérico de 12 dígitos da sua conta AWS Academy.

> [!WARNING]
> Como as chaves temporárias da AWS Academy expiram após algumas horas, lembre-se de atualizar os 3 secrets de autenticação (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN`) dentro do ambiente **`AWS_ACADEMY`** sempre que reiniciar o laboratório antes de realizar novos merges na `master`.

### Passo 2: Executar o Deploy Automático
1.  Realize o merge de qualquer PR ou faça um push direto para a branch `master`.
2.  A esteira **🚀 Deploy Automático na AWS (CD)** iniciará automaticamente.
3.  Ela executará os seguintes passos de forma 100% autônoma:
    *   Garantirá que o bucket S3 de estado do Terraform exista.
    *   Executará o `terraform plan` para exibição de plano das alterações nos logs.
    *   Executará o `terraform apply` para provisionamento do RDS e EKS.
    *   Compilará a aplicação Java com o Maven.
    *   Enviará a imagem Docker ao ECR da AWS.
    *   Substituirá os placeholders de imagem/RDS e aplicará os recursos no EKS.

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
    cd infra/bootstrap
    echo 'account_id = "<AWS_ACCOUNT_ID>"' > terraform.tfvars
    terraform init
    terraform apply -auto-approve
    ```
2.  Retorne à pasta principal do Terraform para subir o EKS e o RDS:
    ```powershell
    cd ..
    echo 'bucket = "ofisy-tfstate-<AWS_ACCOUNT_ID>"' > backend.hcl
    echo 'account_id = "<AWS_ACCOUNT_ID>"' > terraform.tfvars
    echo 'role_name  = "LabRole"' >> terraform.tfvars
    
    terraform init "-backend-config=backend.hcl"
    terraform apply -auto-approve
    ```

### Passo 3: Enviar a imagem e aplicar os manifestos
1.  Conecte seu `kubectl` ao cluster EKS criado:
    ```powershell
    aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster
    ```
2.  Insira o endpoint do banco RDS (endereço retornado pelo Terraform `rds_address`) no arquivo `k8s/configmap.yml` no campo `POSTGRES_HOST`.
3.  Autentique o Docker no ECR, compile a aplicação e faça o push:
    ```powershell
    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
    .\mvnw.cmd clean package -DskipTests
    docker build -t ofisy-app:latest .
    docker tag ofisy-app:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest
    docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ofisy-ecr:latest
    ```
4.  Crie os secrets e aplique os manifestos no cluster:
    ```powershell
    # Cria o secret ofisy-secret via linha de comando no cluster:
    kubectl create secret generic ofisy-secret `
      --from-literal=POSTGRES_USER=ofisy_user `
      --from-literal=POSTGRES_PASSWORD=<SUA_SENHA_RDS> `
      --from-literal=JWT_SECRET=<SEU_JWT_SECRET>

    # Aplica os manifestos da aplicação:
    kubectl apply -f k8s/configmap.yml
    kubectl apply -f k8s/deployment.yml
    kubectl apply -f k8s/service.yml
    kubectl apply -f k8s/hpa.yml
    ```

---

## ⚠️ 5. Limpeza e Destruição (Economia de Créditos)

**Importante:** Os recursos na AWS gerados por esse deploy consomem créditos do seu laboratório. Destrua-os sempre que terminar as apresentações ou avaliações.

### Método A: Via GitHub Actions (Recomendado)
1.  Garanta que as suas chaves da AWS Academy nos Secrets do GitHub estejam atualizadas e válidas.
2.  Acesse a aba **Actions** no seu repositório do GitHub.
3.  No menu lateral esquerdo, selecione a esteira **⚠️ Destruir Infraestrutura AWS**.
4.  Clique no botão **Run workflow** à direita e confirme a execução.
5.  O workflow se conectará ao cluster, deletará os serviços Kubernetes públicos (ELB/ENIs) e destruirá a infraestrutura Terraform de forma totalmente limpa.

### Método B: Via CLI Local
No terminal do PowerShell autenticado, entre na pasta do Terraform e execute a destruição:
```powershell
cd infra
terraform destroy -auto-approve
```
