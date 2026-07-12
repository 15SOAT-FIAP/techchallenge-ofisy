# Guia de Execução e Deploy — Local & AWS Cloud (Fase 2)

Procedimentos para executar, testar e fazer deploy da aplicação **Ofisy** em ambiente local (Docker Compose) e em produção na AWS (via GitHub Actions ou linha de comando).

---

## 1. Execução local rápida (Docker Compose)

Para testes rápidos com aplicação e banco rodando em containers integrados:

1.  Acesse a pasta raiz do projeto.
2.  Copie o arquivo de exemplo de variáveis de ambiente:
    ```bash
    cp env.example .env
    ```
3.  Suba os containers:
    ```bash
    docker compose up --build
    ```
4.  O Swagger fica acessível em: `http://localhost:8080/swagger-ui/index.html`

---

## 2. Deploy na AWS via GitHub Actions (automático)

Esse método automatiza o planejamento do Terraform (`terraform plan`), o provisionamento (`terraform apply`), o build do código e o deploy no cluster EKS da AWS. É acionado automaticamente a cada push ou merge na branch `master`.

### Passo 1: configurar o Environment e Secrets no GitHub

Como a esteira roda de forma autônoma, crie um ambiente (**Environment**) no GitHub chamado **`AWS_ACADEMY`** e salve as credenciais lá como **Environment Secrets**:

1. No GitHub, acesse o repositório e vá em **Settings** > **Environments** > **New environment**.
2. Nomeie o ambiente como **`AWS_ACADEMY`** e clique em **Configure environment**.
3. Na seção **Environment secrets**, clique em **Add secret** para adicionar as seguintes chaves:
    *   `AWS_ACCESS_KEY_ID`: a *AWS Access Key ID* (painel *AWS Details* do Learner Lab).
    *   `AWS_SECRET_ACCESS_KEY`: a *AWS Secret Access Key*.
    *   `AWS_SESSION_TOKEN`: o *AWS Session Token*.
    *   `AWS_ACCOUNT_ID`: o ID numérico de 12 dígitos da conta AWS Academy.

> [!WARNING]
> As chaves temporárias da AWS Academy expiram após algumas horas. Atualize os 3 secrets de autenticação (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN`) no ambiente **`AWS_ACADEMY`** sempre que reiniciar o laboratório antes de novos merges na `master`.

### Passo 2: executar o deploy automático

1.  Faça o merge de um PR ou push direto para a branch `master`.
2.  A esteira **Deploy Automático na AWS (CD)** inicia automaticamente e executa:
    *   Garante que o bucket S3 de estado do Terraform existe.
    *   Executa `terraform plan` para exibir o plano de alterações nos logs.
    *   Executa `terraform apply` para provisionar RDS e EKS.
    *   Compila a aplicação Java com Maven.
    *   Envia a imagem Docker ao ECR da AWS.
    *   Substitui os placeholders de imagem/RDS e aplica os recursos no EKS.

---

## 3. Deploy na AWS via CLI (manual)

Para provisionar e fazer deploy manualmente pela sua máquina:

### Passo 1: autenticar o terminal local

Cole as credenciais do AWS Academy formatadas para o PowerShell:
```powershell
$env:AWS_ACCESS_KEY_ID="SUA_KEY"
$env:AWS_SECRET_ACCESS_KEY="SUA_SECRET"
$env:AWS_SESSION_TOKEN="SEU_TOKEN"
```

### Passo 2: provisionar a infraestrutura (Terraform)

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
    # Busca os nomes dinâmicos das roles do EKS do AWS Academy
    $clusterRole = aws iam list-roles --query "Roles[?contains(RoleName, 'LabEksClusterRole')].RoleName" --output text
    $nodeRole = aws iam list-roles --query "Roles[?contains(RoleName, 'LabEksNodeRole')].RoleName" --output text

    echo 'bucket = "ofisy-tfstate-<AWS_ACCOUNT_ID>"' > backend.hcl
    echo "account_id = `"<AWS_ACCOUNT_ID>`"" > terraform.tfvars
    echo "role_name  = `"LabRole`"" >> terraform.tfvars
    echo "db_password = `"ofisy_pass`"" >> terraform.tfvars
    echo "eks_cluster_role = `"$clusterRole`"" >> terraform.tfvars
    echo "eks_node_role = `"$nodeRole`"" >> terraform.tfvars

    terraform init "-backend-config=backend.hcl"
    terraform apply -auto-approve
    ```

### Passo 3: enviar a imagem e aplicar os manifestos

1.  Conecte o `kubectl` ao cluster EKS criado:
    ```powershell
    aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster
    ```
2.  Insira o endpoint do banco RDS (retornado pelo Terraform como `rds_address`) no campo `POSTGRES_HOST` de `k8s/configmap.yml`.
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

## 4. Limpeza e destruição (economia de créditos)

Os recursos gerados por esse deploy na AWS consomem créditos do laboratório. Destrua-os sempre que terminar apresentações ou avaliações.

### Método A: via GitHub Actions (recomendado)

1.  Garanta que as chaves da AWS Academy nos Secrets do GitHub estejam atualizadas e válidas.
2.  Acesse a aba **Actions** no repositório do GitHub.
3.  No menu lateral, selecione a esteira **Destruir Infraestrutura AWS**.
4.  Clique em **Run workflow** e confirme a execução.
5.  O workflow se conecta ao cluster, deleta os serviços Kubernetes públicos (ELB/ENIs) e destrói a infraestrutura Terraform.

### Método B: via CLI local

No terminal do PowerShell autenticado, entre na pasta do Terraform e execute a destruição:
```powershell
cd infra
terraform destroy -auto-approve
```