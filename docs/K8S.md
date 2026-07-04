# Kubernetes Local com Minikube

Manual para a execução da aplicação localmente num cluster Kubernetes usando Minikube.

---

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

---

## Estrutura dos manifestos

```
k8s/
├── app/
│   ├── configmap.yml    # variáveis de ambiente da aplicação
│   ├── secret.yml       # template de secrets (não versionar valores reais)
│   ├── deployment.yml   # deployment da aplicação com probes e resources
│   ├── service.yml      # NodePort expondo a porta 30080
│   └── hpa.yml          # HorizontalPodAutoscaler baseado em CPU
└── db/
    ├── configmap.yml    # variáveis de ambiente do banco
    ├── secret.yml       # template de secrets do banco
    ├── statefulset.yml  # StatefulSet do PostgreSQL
    ├── service.yml      # Service headless interno do banco
    └── pvc.yml          # PersistentVolumeClaim para os dados do banco
```

---

## Passo a passo

### 1. Iniciar o Minikube

```bash
minikube start
minikube addons enable metrics-server
```

O metrics-server é necessário para o HPA conseguir coletar as metricas e escalar os pods de acordo.

### 2. Apontar o Docker para o contexto do Minikube

```bash
eval $(minikube docker-env)
```

> Esse comando precisa executar em cada novo terminal. Sem ele, a imagem buildada fica no nosso OS e não dentro do minikube.

### 3. Buildar a imagem da aplicação

```bash
docker build -t ofisy-app:latest .
```

### 4. Criar os secrets

Os arquivos `secret.yml` usam apenas placeholders e não devem ser aplicados diretamente, no futuro será feito o replace na pipeline, mas por enquanto precisa criar manualmente via `kubectl`:

```bash
kubectl create secret generic db-secret \
  --from-literal=POSTGRES_USER=postgres \
  --from-literal=POSTGRES_PASSWORD=postgres

kubectl create secret generic ofisy-secret \
  --from-literal=POSTGRES_USER=postgres \
  --from-literal=POSTGRES_PASSWORD=postgres \
  --from-literal=JWT_SECRET=seu_jwt_secret
```

> Se os secrets já forem criados numa execução anterior, não é necessário recriar.

### 5. Aplicar os manifestos do banco

```bash
kubectl apply -f k8s/db/configmap.yml \
              -f k8s/db/pvc.yml \
              -f k8s/db/service.yml \
              -f k8s/db/statefulset.yml
```

Aguarde o pod do banco estar pronto, pode acompanhar com `kubectl get pods --watch`. O pod do banco estará pronto quando o STATUS for `Running` e o READY for `1/1`.

### 6. Aplicar os manifestos da aplicação

```bash
kubectl apply -f k8s/app/configmap.yml \
              -f k8s/app/deployment.yml \
              -f k8s/app/service.yml \
              -f k8s/app/hpa.yml
```

### 7. Verificar o status

```bash
kubectl get pods,services,hpa
kubectl top pods
```

### 8. Acessar a aplicação

```bash
minikube service ofisy-service --url
```

O comando retorna a URL com o NodePort para acessar a aplicação. A documentação Swagger estará disponível em `/swagger-ui.html`.

---

## Observações

### Secrets não são versionados
Os arquivos `secret.yml` existem apenas como documentação da estrutura esperada. Nunca commit valores reais de secrets.

### HPA e memória JVM
O HPA está configurado para escalar apenas por CPU. Escalar por memória com JVM é problemático, pois a JVM reserva heap na inicialização e raramente libera, fazendo o HPA escalar indefinidamente sem necessidade.

### Imagem local
O `imagePullPolicy: Never` no deployment garante que o k8s use a imagem local buildada no contexto do Minikube. Ao migrar para produção precisa alterar para `IfNotPresent` e configurar o ECR com a imagem.

### Banco de dados
O PostgreSQL futuramente será migrado para um RDS e deixará de ser gerenciado pelo k8s. O StatefulSet e PVC serão removidos, e a connection string passará a ser configurada diretamente nos secrets da aplicação.

---

## Parar o ambiente

```bash
minikube stop
```

Os recursos criados (pods, services, secrets) são preservados e restaurados na próxima vez que o Minikube iniciar.

Para destruir tudo:

```bash
minikube delete
```