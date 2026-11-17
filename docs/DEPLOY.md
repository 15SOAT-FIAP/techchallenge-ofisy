# Guia de Execução e Deploy - Local & AWS Cloud

Procedimentos para executar a aplicação **Ofisy** (core) localmente com Docker Compose e para fazer o deploy no cluster EKS da AWS pelos workflows do GitHub Actions.

---

## 1. Execução local (Docker Compose)

Para subir o core com o banco e o LocalStack em containers:

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

Para rodar a aplicação na IDE, com só o banco e o LocalStack no Docker, veja o [README](../README.md#subindo-a-aplicação).

---

## 2. Infraestrutura

O deploy do core não provisiona infraestrutura. Ele espera que já existam:

| Recurso | Nome | Repositório |
|---|---|---|
| Cluster EKS | `ms-ofisy-core-cluster` | `techchallenge-ofisy-eks-infra` (`eks/core`) |
| ECR | `ms-ofisy-core-ecr` | `techchallenge-ofisy-eks-infra` (`eks/core`) |
| RDS PostgreSQL | `ms-ofisy-core-db` | `techchallenge-ofisy-db-infra` (`core`) |
| Tópico SNS e fila SQS | `ofisy-service-order-events.fifo` e `ofisy-service-order.fifo` | `techchallenge-ofisy-eks-infra` (`messaging`) |

O API Gateway, que expõe o core pelo NLB interno, é provisionado depois do deploy de todos os serviços. Os recursos e a ordem entre os repositórios estão em [INFRASTRUCTURE.md](INFRASTRUCTURE.md).

---

## 3. Deploy na AWS

### Pelo ofisy-platform (padrão)

O AWS Academy expira a sessão a cada 4 horas, então o ambiente inteiro é recriado com frequência. O workflow **Subir ambiente** (`up.yml`) do [ofisy-platform](https://github.com/15SOAT-FIAP/ofisy-platform) provisiona a infraestrutura e chama o `cd.yml` deste repositório como reusable workflow (`workflow_call`), na ordem certa. O workflow **Destruir ambiente** (`down.yml`) faz o caminho inverso e chama o `destroy-app.yml`.

### Só o core

Para implantar só o core, por exemplo depois de um merge, rode o workflow **Deploy Manual na AWS (CD)** (`.github/workflows/cd.yml`) na aba **Actions**, informando a branch, tag ou commit (padrão `master`). O CD não roda a cada push: o CI (`ci.yml`) já builda e testa cada push e Pull Request.

O workflow:

1. busca o endereço do RDS `ms-ofisy-core-db` e espera ele ficar disponível. Se o banco não existir ou estiver parado, o deploy para com erro;
2. conecta o `kubectl` ao cluster `ms-ofisy-core-cluster` e instala o Metrics Server, usado pelo HPA;
3. builda o JAR e a imagem e envia ao ECR `ms-ofisy-core-ecr` com a tag do commit curto. Se a tag já existir, reaproveita a imagem, porque o ECR não aceita sobrescrever tags;
4. substitui os placeholders dos manifestos de `k8s/` (endereço do RDS, tag da imagem e secrets em base64), aplica no cluster e espera o rollout do `ofisy-deployment`.

### Secrets

O `cd.yml` e o `destroy-app.yml` rodam no environment **`AWS_ACADEMY`** e usam os secrets da organização `15SOAT-FIAP`:

| Secret | Uso |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Credenciais da sessão do AWS Academy |
| `AWS_ACCOUNT_ID` | Conta do ECR |
| `DB_PASSWORD` | Senha do usuário `admin_db` do RDS |
| `JWT_SECRET` | Assinatura do JWT de funcionário. Precisa ser o mesmo dos microsserviços, que validam esse token |
| `DD_API_KEY` | Métricas do Datadog |

> [!WARNING]
> As chaves temporárias da AWS Academy expiram a cada sessão do laboratório. Atualize `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN` na organização antes de rodar qualquer workflow.

### Manualmente

O passo a passo com `kubectl`, sem o workflow, está em [K8S.md](K8S.md).

---

## 4. Remoção da aplicação

Para remover o core do cluster, rode **Destruir Aplicação no Kubernetes (EKS)** (`.github/workflows/destroy-app.yml`). Ele apaga os recursos de `k8s/`, inclusive o `Service` que cria o NLB, e espera a AWS liberar o Load Balancer. Se o cluster já não existir, termina com sucesso sem fazer nada.

O cluster, o banco e o restante da infraestrutura são destruídos pelos workflows dos repositórios de infraestrutura, ou de uma vez pelo `down.yml` do ofisy-platform. Destrua sempre os recursos quando terminar apresentações ou avaliações, para não consumir os créditos do laboratório.