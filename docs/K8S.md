# Kubernetes no EKS

Manual para publicar e operar o core no cluster EKS sem o workflow de CD. O caminho padrão é o `cd.yml`, descrito em [DEPLOY.md](DEPLOY.md), que faz os mesmos passos.

---

## Pré-requisitos

- Infraestrutura já provisionada: cluster `ms-ofisy-core-cluster` e ECR `ms-ofisy-core-ecr` (`techchallenge-ofisy-eks-infra`), RDS `ms-ofisy-core-db` (`techchallenge-ofisy-db-infra`) e a fila e o tópico da mensageria ([INFRASTRUCTURE.md](INFRASTRUCTURE.md))
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configurado com as credenciais do lab
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Docker](https://docs.docker.com/get-docker/)

---

## Estrutura dos manifestos

```
k8s/
├── configmap.yml    # variáveis não sensíveis: host e nome do banco, perfil k8s e Datadog
├── secret.yml       # template do secret, só com placeholders
├── deployment.yml   # deployment da aplicação com probes, resources e o host do Datadog Agent
├── service.yml      # LoadBalancer que cria um NLB interno na porta 8080
└── hpa.yml          # HorizontalPodAutoscaler por CPU, de 2 a 10 réplicas
```

O banco não roda no cluster: o PostgreSQL fica no RDS, e o acesso ao SNS e ao SQS usa as credenciais da `LabRole` dos nós, sem chaves no manifesto.

---

## Passo a passo

### 1. Apontar o kubectl para o cluster

```bash
aws eks update-kubeconfig --region us-east-1 --name ms-ofisy-core-cluster
kubectl get nodes
```

### 2. Buildar e publicar a imagem no ECR

```bash
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

./mvnw clean package -DskipTests
docker build -t <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ms-ofisy-core-ecr:<IMAGE_TAG> .
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ms-ofisy-core-ecr:<IMAGE_TAG>
```

Para `<IMAGE_TAG>`, use o hash curto do commit (`git rev-parse --short HEAD`), como o CD.

Atenção: o ECR está configurado com `image_tag_mutability = IMMUTABLE`, então `latest` não serve. Cada build exige uma tag nova.

### 3. Preencher os placeholders

Os manifestos trazem placeholders, não aplique direto. Substitua pelos valores reais:

| Arquivo          | Placeholder                                                                            | Valor                                                                 |
|------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `configmap.yml`  | `<RDS_ADDRESS>`                                                                        | Endereço do RDS `ms-ofisy-core-db`                                    |
| `configmap.yml`  | `<IMAGE_TAG>`                                                                          | Tag da imagem, usada como `DD_VERSION`                                |
| `deployment.yml` | `<ECR_REPOSITORY_URL>:<IMAGE_TAG>`                                                     | `<AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/ms-ofisy-core-ecr:<IMAGE_TAG>` |
| `secret.yml`     | `<BASE64_USERNAME>`, `<BASE64_PASSWORD>`, `<BASE64_JWT_SECRET>`, `<BASE64_DD_API_KEY>` | `admin_db`, a senha do RDS, o `JWT_SECRET` e a API Key do Datadog, em base64 |

O endereço do RDS sai de:

```bash
aws rds describe-db-instances --db-instance-identifier ms-ofisy-core-db \
  --query "DBInstances[0].Endpoint.Address" --output text
```

Não faça commit dos manifestos com os valores preenchidos.

### 4. Aplicar os manifestos

```bash
kubectl apply -f k8s/secret.yml \
              -f k8s/configmap.yml \
              -f k8s/deployment.yml \
              -f k8s/service.yml \
              -f k8s/hpa.yml
kubectl rollout status deployment/ofisy-deployment --timeout=300s
```

### 5. Verificar o status

```bash
kubectl get pods,services,hpa
kubectl top pods
```

O pod da aplicação estará pronto quando o STATUS for `Running` e o READY for `1/1`.

### 6. Acessar a aplicação

O NLB criado pelo `ofisy-service` é interno (ver a seção LoadBalancer nas observações), então o hostname só resolve de dentro da VPC. O acesso externo à aplicação, incluindo a documentação Swagger em `/swagger-ui.html`, é feito pela URL do API Gateway. Para testar direto o service durante a operação do cluster, use um port-forward:

```bash
kubectl port-forward service/ofisy-service 8080:8080
```

Se o `Service` for recriado, o NLB muda e o deploy do API Gateway (`techchallenge-ofisy-eks-infra`) precisa rodar de novo.

---

## Observações

### Secrets não são versionados
O `secret.yml` existe apenas como documentação da estrutura esperada. Nunca faça commit de valores reais de secrets.

### HPA e memória JVM
O HPA escala só por CPU de propósito ([ADR-0005](adr/0005-escalar-a-aplicacao-com-hpa-por-cpu.md)). Memória com JVM não funciona bem pra isso: a JVM reserva heap na inicialização e quase nunca libera, então o HPA acabaria escalando sem parar mesmo sem necessidade real.

### Metrics Server
O EKS não vem com metrics-server instalado. Sem ele o HPA fica cego e não escala nada. O CD instala; manualmente:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl patch deployment metrics-server -n kube-system --type='json' \
  -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
```

O `--kubelet-insecure-tls` é necessário porque o kubelet do EKS usa certificado autoassinado.

### Imagem via ECR
`imagePullPolicy: Always` faz o kubelet puxar a imagem do ECR toda vez antes de subir o pod.

### LoadBalancer
`service.yml` usa `type: LoadBalancer` com a anotação `service.beta.kubernetes.io/aws-load-balancer-type: nlb`. Essa anotação é reconhecida pelo cloud provider nativo do EKS e faz o Kubernetes provisionar um Network Load Balancer (NLB) em vez do Classic ELB, que seria o padrão sem ela. É de propósito: o AWS Load Balancer Controller pediria uma IAM policy que o AWS Academy não libera, então usamos essa anotação nativa pra conseguir um NLB sem precisar instalar o controller.

A segunda anotação, `service.beta.kubernetes.io/aws-load-balancer-internal: "true"`, faz o NLB ser criado como interno, sem IP público. Quem entra na aplicação vem pelo API Gateway, que alcança esse NLB por um VPC Link (ver `docs/INFRA-CICD-DIAGRAM.md`). O billing e o notification também chamam o core por esse NLB, de dentro da VPC.

---

## Remover a aplicação

Para limpar a aplicação sem derrubar o cluster, rode o `destroy-app.yml` ([DEPLOY.md](DEPLOY.md#4-remoção-da-aplicação)) ou:

```bash
kubectl delete -f k8s/hpa.yml -f k8s/service.yml -f k8s/deployment.yml -f k8s/configmap.yml -f k8s/secret.yml
```

Antes de destruir o cluster, remova o `Service`: o NLB é criado pelo Kubernetes, fora do Terraform, e ficaria órfão. O destroy do cluster no `techchallenge-ofisy-eks-infra` já apaga os `Service` do tipo `LoadBalancer` antes do `terraform destroy`.