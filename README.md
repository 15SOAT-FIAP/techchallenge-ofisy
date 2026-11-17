# Ofisy

FIAP 15SOAT - Tech Challenge - Grupo 87

Sistema de gestão de oficina automotiva desenvolvido com Spring Boot e DDD.

Este repositório é o **core** da Ofisy: cuida de clientes, veículos, catálogo de serviços, usuários e ordens de serviço, e é o dono do status da OS. Orçamento e pagamento, estoque, execução e notificações ficam em microsserviços próprios, que conversam com o core por eventos no SNS e no SQS ([Repositórios do projeto](#repositórios-do-projeto)).

---
## Estrutura do Projeto

Nas primeiras fases, o projeto foi construído como um **Monolito Modular**, organizado por agregado para facilitar uma futura divisão. Na Fase 4, os contextos de orçamento e pagamento, estoque, execução e notificação foram extraídos para microsserviços, cada um com seu próprio banco ([ADR-0010](docs/adr/0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md)), e o core ficou com os demais.

A estruturação das classes e entidades criadas segue os princípios da **Clean Architecture**, combinados com conceitos de **DDD (Domain-Driven Design)** para a modelagem dos agregados, organizadas nas seguintes camadas:

```
br.com.ofisy
├── domain/                       # Regras de negócio e entidades ricas, sem dependências externas
│   ├── <agregado>/                   # Entidade, Value Objects, interface de repositório (porta) e exceções por agregado
│   └── messaging/                    # Evento do outbox e evento processado
├── application/                  # Casos de uso da aplicação (orquestração das regras de negócio)
│   └── <agregado>/
│       └── <caso-de-uso>/            # Interface do UseCase + Service com a implementação do caso de uso
├── adapters/                     # Implementações que conectam o domínio/aplicação ao mundo externo
│   ├── controllers/                  # Adaptadores de entrada: Controllers REST, contrato da API (Api) e DTOs de request/response
│   │   └── <agregado>/
│   │       └── dto/
│   ├── gateways/                     # Adaptadores de saída: implementação dos repositórios do domínio (Spring Data JPA)
│   │   └── <agregado>/                   # Entity JPA, Mapper, RepositoryImpl (implementa a porta do domínio) e interface Spring Data
│   ├── presenters/                   # Conversão das entidades de domínio para os DTOs de resposta
│   │   └── <agregado>/
│   ├── listeners/                    # Adaptadores de entrada: consumo da fila SQS e um handler por tipo de evento
│   ├── publishers/                   # Adaptadores de saída: publicação do outbox no SNS
│   └── tracing/                      # Propagação do contexto do trace pelo outbox e pela fila
├── config/                       # Configurações transversais (segurança, JWT, Swagger, tratamento global de exceções)
└── shared/                       # Utilitários compartilhados entre camadas (ex.: filtros de segurança, serviço de JWT)
```

---

## Stack do Projeto

A combinação dessas tecnologias permite a construção de um backend robusto e de fácil manutenção, a execução local do ambiente de desenvolvimento de forma facilitada para o time e para avaliação posterior do projeto e a futura evolução da aplicação conforme as próximas fases do projeto.

| Tecnologia  | Versão   | Justificativa                                                                                                                                                                                                                                                                                                                                                                                                                |
|-------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java        | 21 (LTS) | Linguagem conhecida e utilizada por todos do grupo. Maturidade, robustez e suporte da comunidade. O JDK 21 oferece melhorias de desempenho e segurança, garantindo estabilidade e suporte de longo prazo, além de compatibilidade com a versão do Spring Boot escolhida.                                                                                                                                                     |
| Spring Boot | 4.0.5    | Totalmente compatível com Java 21. Inclui melhorias de performance, atualizações do Spring Framework 7 e suporte a observabilidade e containers. O uso de dependências como o Spring Data JPA facilita o mapeamento entre o domínio da aplicação e a estrutura de dados.                                                                                                                                                     |
| PostgreSQL  | 16       | Banco de dados relacional escolhido devido à natureza monolítica da aplicação e à estrutura de entidades com forte relacionamento entre si. O modelo relacional facilita a representação dessas relações por meio de chaves estrangeiras, garantindo integridade entre os dados. Solução amplamente utilizada em sistemas corporativos, com versão estável e melhorias de desempenho, paralelismo e otimização de consultas. |
| Spring Cloud AWS | 4.2.0 | Integração com o Amazon SNS e o Amazon SQS, usados na comunicação por eventos com os microsserviços. |
| LocalStack  | 4.13.1   | Emulação do SNS e do SQS no ambiente local e nos testes de integração, sem depender de uma conta AWS. |
| Maven       | 3.9.x    | Escolhido como ferramenta de gerenciamento de dependências e build do projeto pela facilidade de utilização e praticidade.                                                                                                                                                                                                                                                                                                   |
| Docker      | —        | Necessário para containerização da aplicação e de seus serviços dependentes, garantindo um ambiente de execução padronizado e isolado, juntamente com o banco de dados através do uso do Docker Compose. Trata-se também de uma exigência para a entrega do projeto.                                                                                                                                                         |

---

## Dependências

| Dependência                | Descrição                                                                                                                                                                                          | Artefato                                                                                                               |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| Spring Web	                 | Dependência para criação de APIs RESTful, incluindo o servidor web (Tomcat), mapeamento de rotas e controllers	                                                                                     | `spring-boot-starter-web`                                                                                              |
| Spring Cloud AWS SQS/SNS   | Dependência para a comunicação com os microsserviços: consumo da fila via `@SqsListener` e publicação dos eventos do outbox no tópico SNS                                                          | `spring-cloud-aws-starter-sqs`, `spring-cloud-aws-starter-sns`                                                         |
| Spring Data JPA	            | Dependência para gerenciar a comunicação com o banco de dados. Além de acelerar o desenvolvimento e reduzir o código boilerplate, permite a utilização de objetos ORM dentre outras facilidades    | 	`spring-boot-starter-data-jpa`                                                                                         |
| Spring Security	            | Dependência para tratar autenticação e autorização da aplicação                                                                                                                                    | 	`spring-boot-starter-security`                                                                                         |
| Spring Security JWT        | Dependência para tratar autenticação e autorização da aplicação. Exigência de utilização de JWT para entrega do projeto em APIs administrativas.                                                   | 	`jjwt-api`, `jjwt-impl`,`jjwt-jackson`                                                                                 |
| OpenAPI / Swagger	          | Dependência para documentação e exposição das APIs/rotas criadas para o projeto via Swagger nesta fase 1.	                                                                                          | `springdoc-openapi-starter-webmvc-ui`                                                                                  |
| Spring Validation          | 	Dependência para validação de DTOs e beans	                                                                                                                                                         | `spring-boot-starter-validation`                                                                                       |
| PostgreSQL	                 | Dependência para o banco de dados PostgreSQL                                                                                                                                                       | 	`postgresql`                                                                                                           |
| Docker Compose	             | Dependência para permitir utilização de docker compose para aplicação                                                                                                                              | 	`spring-boot-docker-compose`                                                                                           |
| Lombok                     | 	Dependência para permitir redução de código boilerplate através de anotações, gerando automaticamente getters, setters, construtores etc.	                                                          | `lombok`                                                                                                               |
| Spring Boot DevTools       | Dependência para acelerar o ciclo de desenvolvimento local, com reinício automático da aplicação a cada alteração de código                                                                        | `spring-boot-devtools`                                                                                                 |
| Spring Actuator            | 	Dependência utilizada para obter endpoints de monitoramento e observabilidade da aplicação, como verificação de saúde (/actuator/health), métricas entre outras informações	                        | `spring-boot-starter-actuator`                                                                                         |
| Micrometer Datadog         | Envia as métricas da aplicação, inclusive as métricas de negócio das ordens de serviço, direto para a API do Datadog | `micrometer-registry-datadog` |
| Logstash Logback Encoder   | Gera os logs em JSON, com os campos de correlação com o trace, para o Datadog | `logstash-logback-encoder` |
| OpenTelemetry API          | Captura e restaura o contexto do trace pelo outbox e pela fila, conectada ao `dd-java-agent` | `opentelemetry-api` |
| Spring Boot Test           | 	Dependência para escrita de testes automatizados, incluindo suporte a testes unitários e de integração com JUnit e Mockito	                                                                         | `spring-boot-starter-test`                                                                                             |
| Spring Boot Webmvc Test    | Dependência que permite testar de forma isolada a camada de Controller (Web), simulando requisições HTTP, validando rotas, parâmetros de entrada e o JSON de retorno sem subir o servidor completo | `spring-boot-webmvc-test`                                                                                              |
| Spring Security Test       | 	Dependência para testes de endpoints protegidos, permitindo simular usuários autenticados e verificar comportamentos de segurança nos testes	                                                       | `spring-security-test`                                                                                                 |
| Flyway Core                | Dependência principal do framework de migração de banco de dados, responsável por gerenciar o histórico de versões, executar scripts SQL automaticamente e garantir a integridade do esquema       | `spring-boot-starter-flyway`                                                                                           |
| Flyway Database PostgreSQL | Extensão específica do Flyway que adiciona suporte completo às funcionalidades do PostgreSQL, permitindo que o framework se comunique corretamente com o dialeto e driver desse banco              | `flyway-database-postgresql`                                                                                           |
| Flyway Test                | Dependência com suporte do Spring Boot ao Flyway em ambiente de testes, utilizada em conjunto com o Testcontainers para garantir que as migrations sejam aplicadas nos testes de integração        | `spring-boot-starter-flyway-test`                                                                                      |
| Testcontainers             | Dependência para criação de containers Docker durante os testes de integração, permitindo subir um banco PostgreSQL e um LocalStack (SNS e SQS) reais de forma isolada e automatizada              | `spring-boot-testcontainers`, `testcontainers-postgresql`, `testcontainers-localstack`, `testcontainers-junit-jupiter` |
| Awaitility                 | Dependência para asserções sobre o processamento assíncrono da fila e do outbox nos testes de integração                                                                                           | `awaitility`                                                                                                           |
| REST Assured               | Dependência para testes de integração de APIs REST, permitindo realizar requisições HTTP e validar respostas de forma fluente e legível                                                            | `rest-assured`                                                                                                         |

---

## Arquivos de Configuração

### Aplicação e Build

- `application.yml` — configurações para subir o servidor em ambiente local, sendo banco via Docker e a aplicação na IDE, além da região AWS e dos nomes da fila e do tópico da mensageria;
- `application-dev.yml` — aponta o SDK da AWS para o LocalStack em `localhost:4566`;
- `application-docker.yml` — configurações para subir o ambiente completo via Docker, com o SDK da AWS apontando para o LocalStack do compose;
- `application-k8s.yml` — configurações do perfil `k8s`, utilizado quando a aplicação é executada no cluster Kubernetes (EKS), incluindo conexão com o banco via `POSTGRES_HOST` e habilitação dos probes de saúde do Actuator para liveness/readiness, além do Swagger agregado com as specs dos microsserviços;
- `logback-spring.xml` — logs em JSON para o Datadog no perfil `k8s` e console padrão nos demais;
- `pom.xml` — dependências do projeto gerenciadas via Maven.

### Docker

- `Dockerfile` — instruções para o Docker buildar a aplicação;
- `compose.yaml` — orquestração dos containers da aplicação, do banco e do LocalStack, permitindo subir o ambiente completo em qualquer lugar, por qualquer pessoa. Segue a nomenclatura mais recente para esse tipo de arquivo;
- `compose.db.yaml` — configurações para subir apenas os containers do banco e do LocalStack para desenvolvimento local;
- `compose.sonar.yaml` — configurações para subir apenas o container do SonarQube para análise de qualidade, vulnerabilidades e cobertura de testes da aplicação;
- `compose.zap.yaml` — configurações para subir apenas o container do OWASP ZAP para análise de vulnerabilidades da aplicação;
- `.dockerignore` — instruções para que o Docker ignore determinados arquivos e pastas durante o build, reduzindo o tamanho da imagem gerada.

### Mensageria

- `localstack/init/ready.d/01-messaging.sh` — cria a fila, a DLQ, o tópico e a fila de inspeção no LocalStack. É o mesmo script usado nos testes de integração.

### Git e Ambiente

- `.gitignore` — instruções para que o Git ignore determinados tipos, pastas e denominações de arquivos;
- `.gitattributes` — normaliza o final de linha (`eol`) dos scripts `mvnw`/`mvnw.cmd` entre diferentes sistemas operacionais;
- `.env` — variáveis sensíveis utilizadas no projeto. Este arquivo **não é versionado**;
- `env.example` — arquivo de exemplo do `.env` para criação do ambiente local.

### CI/CD (GitHub Actions)

- `.github/workflows/ci.yml` — pipeline de Build & Test (CI), executado a cada push/PR;
- `.github/workflows/cd.yml` — pipeline de deploy na AWS EKS (CD), disparado manualmente (`workflow_dispatch`) ou chamado pelo [`ofisy-platform`](https://github.com/15SOAT-FIAP/ofisy-platform) ao subir o ambiente (`workflow_call`);
- `.github/workflows/destroy-app.yml` — pipeline para desinstalar os recursos da aplicação no Kubernetes (EKS), disparado manualmente ou chamado pelo `ofisy-platform` ao destruir o ambiente. Não falha se o cluster já não existir;
- `.github/workflows/notify-pr.yml` — pipeline que notifica a criação/atualização de Pull Requests em um canal do Discord.

---

## Repositórios do projeto

O projeto é dividido em repositórios independentes, cada um com seu próprio ciclo de entrega e CI/CD automatizado:

**Aplicações**

1. **[Core](https://github.com/15SOAT-FIAP/techchallenge-ofisy)** *(este repositório)* — Clientes, veículos, catálogo de serviços, usuários e ordens de serviço. Emite o JWT dos funcionários.
2. **[ms-ofisy-billing](https://github.com/15SOAT-FIAP/ms-ofisy-billing)** — Orçamento e pagamento, com checkout pelo Mercado Pago.
3. **[ms-ofisy-stock](https://github.com/15SOAT-FIAP/ms-ofisy-stock)** — Estoque de peças e insumos e suas movimentações.
4. **[ms-ofisy-execution](https://github.com/15SOAT-FIAP/ms-ofisy-execution)** — Execução dos serviços da OS pelo mecânico, do início à conclusão.
5. **[ms-ofisy-notification](https://github.com/15SOAT-FIAP/ms-ofisy-notification)** — Notificações de estoque, orçamento e pagamento, gravadas no DynamoDB.
6. **[techchallenge-ofisy-auth](https://github.com/15SOAT-FIAP/techchallenge-ofisy-auth)** — Lambda de autenticação de clientes por CPF/CNPJ, com emissão do JWT, e o authorizer do API Gateway.

**Infraestrutura**

7. **[techchallenge-ofisy-eks-infra](https://github.com/15SOAT-FIAP/techchallenge-ofisy-eks-infra)** — Rede, cluster EKS, API Gateway, infraestrutura da Lambda de autenticação e Datadog Agent (Terraform).
8. **[techchallenge-ofisy-db-infra](https://github.com/15SOAT-FIAP/techchallenge-ofisy-db-infra)** — Bancos de dados de todos os serviços: um RDS PostgreSQL por serviço e a tabela DynamoDB do notification (Terraform) ([ADR-0014](docs/adr/0014-manter-a-infraestrutura-de-bancos-em-um-unico-repositorio.md)).
9. **[ofisy-platform](https://github.com/15SOAT-FIAP/ofisy-platform)** — Sobe e derruba o ambiente inteiro na AWS, chamando os workflows de infraestrutura e de deploy na ordem certa.

---

### Manifestos Kubernetes e Testes

- `k8s/` — manifestos Kubernetes (`deployment`, `service`, `configmap`, `secret`, `hpa`) utilizados para executar a aplicação no cluster EKS;
- `k6/` — scripts de teste de carga com [k6](https://k6.io/);
- `zap/` — plano de varredura do OWASP ZAP para análise de vulnerabilidades da aplicação.

---

## Configuração do ambiente para execução

### 1. Clone o repositório do GitHub

via HTTPS
```bash
git clone https://github.com/15SOAT-FIAP/techchallenge-ofisy
```
ou

via SSH
```bash
git clone git@github.com:15SOAT-FIAP/techchallenge-ofisy.git
```

Acesse o repositório clonado.

### 2. Crie o arquivo `.env`

O arquivo `env.example` já contém os valores prontos para um novo ambiente ou avaliação. Para efetuar a cópia, execute o comando:

```bash
cp env.example .env
```

> O `env.example` possui credenciais funcionais para o ambiente de desenvolvimento e avaliação.

---

## Subindo a aplicação

### Opção 1 — Ambiente completo via Docker

Para efetuar a subida do ambiente completo, com banco, LocalStack e aplicação juntos, rode o seguinte comando:

```bash
docker compose up --build
```

### Opção 2 — Desenvolvimento local (banco e LocalStack no Docker + aplicação na IDE)

**Passo 1:** Suba primeiro o banco e o LocalStack via Docker utilizando o comando abaixo:

```bash
docker compose -f compose.db.yaml up -d
```

**Passo 2:** Execute a subida da aplicação:

```bash
./mvnw spring-boot:run
```

> A aplicação usará o perfil `dev` por padrão, conectando ao banco em `localhost:5432` e ao LocalStack (SNS e SQS) em `localhost:4566`. Os dois sobem pelo `compose.db.yaml`.

---

## Acesso a aplicação e Swagger

- A API estará disponível em `http://localhost:8080`.
- A documentação Swagger estará disponível em `http://localhost:8080/swagger-ui.html`.

Em produção, o Swagger do core é **agregado**: pelo seletor de definições no topo do Swagger UI, ele mostra também as APIs do billing, do stock, do execution e do notification. A spec do core vem do próprio `/v3/api-docs`, e a de cada microsserviço vem da rota `/docs/<service>` do API Gateway (configuração em `application-k8s.yml`). O *Try it out* chama as rotas na raiz do Gateway, com o mesmo roteamento e o mesmo authorizer das chamadas normais.

### Login
- Para realizar o login seguir os seguintes passos:
    1. Acessar a API de Login: http://localhost:8080/swagger-ui/index.html#/Autentica%C3%A7%C3%A3o%20via%20JWT%20Token%20para%20APIs%20administrativas
    2. Utilizar os usuários abaixo de acordo com a ROLE disponível, sendo:
        - ADMIN - Usuário administrador
        - ATTENDANT - Usuário atendente
        - MECHANIC - Usuário mecânico
        - STOCKMAN - Usuário almoxarife
    3. Receber o JWT token no login
    4. Adicionar o token no campo Bearer Token do componente Authorize do Swagger
    5. Acessar as APIs que possuem autorização para o usuário logado

O token traz o email do usuário no `sub` e a role na claim `role`. Os microsserviços não têm o banco de usuários, então validam o token com o mesmo `JWT_SECRET` e autorizam pela claim `role`.

As rotas de estoque (`/api/v1/stocks`) e de movimentações (`/api/v1/stock-movements`) não ficam mais neste repositório: elas são servidas pelo [ms-ofisy-stock](https://github.com/15SOAT-FIAP/ms-ofisy-stock). O usuário STOCKMAN continua sendo criado aqui e usa o mesmo token para acessar essas rotas no microsserviço.

As rotas de orçamento (`/api/v1/quotes`) e de cobrança (`/api/v1/payments`) não ficam mais neste repositório: elas são servidas pelo [ms-ofisy-billing](https://github.com/15SOAT-FIAP/ms-ofisy-billing). O core continua dono do status da OS e o atualiza ao receber `QuoteSentForApproval`, `QuoteApproved`, `QuoteCancelled` e `PaymentConfirmed` do Billing. Quando a OS é finalizada, publica `ServiceOrderFinished`, que dispara a cobrança. O `PaymentConfirmed` move a OS para `PAID`, e só a partir daí o veículo pode ser entregue. A autenticação de funcionários continua usando o JWT emitido pelo Core.

As rotas de execução (`/api/v1/service_order_executions`) são servidas pelo [ms-ofisy-execution](https://github.com/15SOAT-FIAP/ms-ofisy-execution). O core move a OS para `IN_PROGRESS` ao receber `ExecutionStarted` e para `FINISHED` ao receber `AllExecutionsCompleted`. Do [ms-ofisy-stock](https://github.com/15SOAT-FIAP/ms-ofisy-stock), o core consome o `StockRefunded`, que cancela a OS quando o orçamento é reprovado. A autenticação de funcionários continua usando o JWT emitido pelo Core.

As rotas de notificações (`/api/v1/notifications`) são servidas pelo [ms-ofisy-notification](https://github.com/15SOAT-FIAP/ms-ofisy-notification). O microsserviço cria o aviso de orçamento ao receber `QuoteSentForApproval` e os avisos de pagamento ao receber `PaymentCheckoutCreated`, `PaymentConfirmed` e `PaymentFailed`, todos do Billing. Para saber o cliente da OS, ele consulta o core em `GET /api/v1/service-orders/{id}/customer`. A autenticação de funcionários continua usando o JWT emitido pelo Core.

### Usuários disponíveis para utilização:
#### ROLE ADMIN
    - username: admin@ofisy.com
    - senha: Admin@1234
#### ROLE ATTENDANT
    - username: atendimento@ofisy.com
    - senha: Atende@1234
#### ROLE MECHANIC
    - username: mecanico@ofisy.com
    - senha: Graxa@1234
#### ROLE STOCKMAN
    - username: almoxarife@ofisy.com
    - senha: Estoque@1234

---

## Variáveis de ambiente

| Variável            | Descrição                           |
|---------------------|-------------------------------------|
| `POSTGRES_DB`       | Nome do banco de dados              |
| `POSTGRES_USER`     | Usuário do banco                    |
| `POSTGRES_PASSWORD` | Senha do banco                      |
| `JWT_SECRET`        | Secret para assinatura do token JWT. Os microsserviços usam o mesmo valor para validar o token |
| `ADMIN_LOGIN`, `ADMIN_PASSWORD` | Usuário e senha usados pelo OWASP ZAP (`compose.zap.yaml`) para autenticar na varredura |
| `POSTGRES_HOST`     | Host do banco no perfil `k8s`       |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (`k8s` no cluster; `dev` por padrão no ambiente local) |
| `DD_API_KEY`, `DD_METRICS_ENABLED` | Exportação de métricas para o Datadog (desligada por padrão) |
| `DD_TRACE_OTEL_ENABLED` | Conecta a API do OpenTelemetry ao `dd-java-agent`, necessária para o trace seguir pelo outbox |

As demais variáveis do Datadog (`DD_SERVICE`, `DD_ENV`, `DD_VERSION`, `DD_LOGS_INJECTION` e `DD_TRACE_AGENT_PORT`) estão no `k8s/configmap.yml` e descritas em [Observabilidade com Datadog](docs/OBSERVABILITY.md).

---

## Observações de segurança

- O arquivo `.env` está no `.gitignore` e **não deve ser versionado**
- As instruções para criação do `.env` estão no corpo deste documento.

---

## Documentação Adicional

### Arquitetura
- **[Arquitetura](docs/ARCHITECTURE.md)** - Visão do sistema inteiro: serviços na AWS, mapa de eventos entre os microsserviços e máquinas de estado da saga
- **[Diagrama de Componentes](docs/COMPONENT-DIAGRAM.md)** - Componentes do backend do core e como se relacionam (padrão C4)
- **[Architecture Decision Records (ADRs)](docs/adr/README.md)** - Registro das decisões arquiteturais do projeto, sua motivação e consequências
- **[Requests for Comments (RFCs)](docs/rfc/README.md)** - Propostas de mudança discutidas pelo time antes de virarem decisão
- **[Diagrama de Sequência](docs/SEQUENCE-DIAGRAM.md)** - Saga da ordem de serviço entre o core e os microsserviços: caminho feliz e compensações
- **[Mensageria](docs/MESSAGING.md)** - Comunicação com os microsserviços por SNS e SQS: envelope dos eventos, outbox e listener
- **[Documentação Banco de Dados](docs/DATABASE.md)** - Justificativa formal sobre a escolha do banco de dados, modelo relacional, diagramas ER e explicação dos relacionamentos.

### Deploy e Infraestrutura
- **[Guia de Deploy](docs/DEPLOY.md)** - Instruções completas para execução local (Docker Compose) e deploy em nuvem AWS (CI/CD ou CLI)
- **[Infraestrutura AWS com Terraform](docs/INFRASTRUCTURE.md)** - Recursos AWS provisionados, arquitetura de rede e passo a passo para inicializar, validar e aplicar a infraestrutura
- **[Kubernetes no EKS](docs/K8S.md)** - Manual para publicar e operar a aplicação no cluster EKS provisionado via Terraform
- **[Diagrama de Infraestrutura e CI/CD](docs/INFRA-CICD-DIAGRAM.md)** - Infraestrutura AWS dos cinco serviços e o fluxo de CI/CD orquestrado pelo ofisy-platform
- **[Observabilidade com Datadog](docs/OBSERVABILITY.md)** - Instrumentação da aplicação (traces, logs correlacionados e métricas), rastreamento distribuído entre os microsserviços, dashboards e monitors

### Qualidade e Testes
- **[Guia de Testes](docs/TESTING.md)** - Instruções para executar análises de cobertura de testes e segurança
- **[Teste de Carga (k6)](docs/LOAD-TEST.md)** - Como executar o script de teste de carga para validar a estabilidade da API sob volume crescente de requisições

### Banco de Dados
- **[Guia de Flyway](docs/FLYWAY.md)** - Instruções para criar e executar migrations