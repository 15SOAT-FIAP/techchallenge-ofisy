# Kubernetes no EKS

Manual para publicar a aplicação no cluster EKS provisionado via Terraform (veja `docs/INFRASTRUCTURE.md`).

---

## Pré-requisitos

- Infraestrutura já provisionada (`infra/terraform`), com cluster EKS, RDS e ECR de pé
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configurado com as credenciais do lab
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Docker](https://docs.docker.com/get-docker/)

---

## Estrutura dos manifestos

```
k8s/
├── configmap.yml    # variáveis de ambiente da aplicação (host do RDS, profile)
├── secret.yml       # template de secrets (não versionar valores reais)
├── deployment.yml   # deployment da aplicação com probes e resources
├── service.yml      # LoadBalancer expondo a porta 8080
└── hpa.yml          # HorizontalPodAutoscaler baseado em CPU
```

O banco saiu do cluster. PostgreSQL agora roda no RDS criado pelo Terraform, e o StatefulSet e o PVC que existiam aqui foram removidos.

---

## Passo a passo

### 1. Apontar o kubectl para o cluster

```bash
aws eks update-kubeconfig --region us-east-1 --name ofisy-cluster
kubectl get nodes
```

Se a infra ainda não foi provisionada, comece por `docs/INFRASTRUCTURE.md`.

### 2. Buildar e publicar a imagem no ECR

```bash
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin <ECR_REPOSITORY_URL>

docker build -t <ECR_REPOSITORY_URL>:<IMAGE_TAG> .
docker push <ECR_REPOSITORY_URL>:<IMAGE_TAG>
```

`<ECR_REPOSITORY_URL>` vem do output `ecr_repository_url` do Terraform. Para `<IMAGE_TAG>`, o hash curto do commit funciona bem (`git rev-parse --short HEAD`).

Atenção: o ECR está configurado com `image_tag_mutability = IMMUTABLE`, então `latest` não serve. Cada build exige uma tag nova.

### 3. Criar os secrets

Os arquivos `secret.yml` trazem só placeholders, não aplique direto. O replace ainda é manual, deve entrar numa pipeline de CD mais pra frente.

```bash
kubectl create secret generic ofisy-secret \
  --from-literal=POSTGRES_USER=admin \
  --from-literal=POSTGRES_PASSWORD=<DB_PASSWORD> \
  --from-literal=JWT_SECRET=<JWT_SECRET>
```

Se o secret já existir de uma execução anterior, não precisa recriar.

### 4. Preencher os placeholders e aplicar os manifestos

Antes de aplicar, abra `k8s/deployment.yml` e `k8s/configmap.yml` e substitua os placeholders pelos valores reais:

- `deployment.yml`: `<ECR_REPOSITORY_URL>` e `<IMAGE_TAG>`, os mesmos valores usados no build da imagem no passo 2
- `configmap.yml`: `<RDS_ADDRESS>`, o output `rds_address` do Terraform

```bash
kubectl apply -f k8s/configmap.yml \
              -f k8s/deployment.yml \
              -f k8s/service.yml \
              -f k8s/hpa.yml
```

### 5. Verificar o status

```bash
kubectl get pods,services,hpa
kubectl top pods
```

O pod da aplicação estará pronto quando o STATUS for `Running` e o READY for `1/1`.

### 6. Acessar a aplicação

```bash
kubectl get service ofisy-service
```

O `EXTERNAL-IP` (hostname do Load Balancer) demora alguns minutos pra aparecer depois do apply, não se assuste se vier vazio de início. A documentação Swagger fica em `/swagger-ui.html`.

---

## Observações

### Secrets não são versionados
Os arquivos `secret.yml` existem apenas como documentação da estrutura esperada. Nunca commit valores reais de secrets.

### HPA e memória JVM
O HPA escala só por CPU de propósito. Memória com JVM não funciona bem pra isso: a JVM reserva heap na inicialização e quase nunca libera, então o HPA acabaria escalando sem parar mesmo sem necessidade real.

### Metrics Server
O EKS não vem com metrics-server instalado. Sem ele o HPA fica cego e não escala nada:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Imagem via ECR
`imagePullPolicy: Always` faz o kubelet puxar a imagem do ECR toda vez antes de subir o pod. No Minikube era `Never`, porque a imagem já estava local.

### LoadBalancer
`service.yml` usa `type: LoadBalancer` com a anotação `service.beta.kubernetes.io/aws-load-balancer-type: nlb`. Essa anotação é reconhecida pelo cloud provider nativo do EKS e faz o Kubernetes provisionar um Network Load Balancer (NLB) em vez do Classic ELB, que seria o padrão sem ela. É de propósito: o AWS Load Balancer Controller pediria uma IAM policy que o AWS Academy não libera, então usamos essa anotação nativa pra conseguir um NLB sem precisar instalar o controller.

### Banco de dados
PostgreSQL roda no RDS (`infra/terraform/rds.tf`), fora do cluster. O `POSTGRES_HOST` do ConfigMap aponta pro endpoint do RDS; hoje isso é preenchido na mão a partir do output do Terraform.

---

## Destruir os recursos

Os manifestos do Kubernetes não precisam ser removidos manualmente. Ao destruir o cluster (`terraform destroy` em `infra/terraform`), os pods, services e demais recursos somem junto.

Se quiser apenas limpar a aplicação sem derrubar o cluster:

```bash
kubectl delete -f k8s/deployment.yml -f k8s/service.yml -f k8s/hpa.yml -f k8s/configmap.yml
kubectl delete secret ofisy-secret
```