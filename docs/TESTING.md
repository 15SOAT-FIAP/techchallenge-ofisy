# Guia de Testes e Análises

## Testes de Integração

Os testes de integração usam **Testcontainers** para subir um container PostgreSQL automaticamente via JUnit, sem necessidade de Docker Compose manual ou configuração externa. O Maven Failsafe Plugin os executa separadamente dos testes unitários.

### Pré-requisitos

- Docker rodando localmente

### Estrutura dos testes

| Arquivo                                                     | Camada                   | Módulo           |
|-------------------------------------------------------------|--------------------------|------------------|
| `application/serviceorder/ServiceOrderServiceIT.java`       | Service (injeção direta) | Ordem de Serviço |
| `application/quote/QuoteServiceIT.java`                     | Service (injeção direta) | Orçamento        |
| `application/stock/StockServiceIT.java`                     | Service (injeção direta) | Estoque          |
| `interfaces/api/serviceorder/ServiceOrderControllerIT.java` | HTTP via REST-assured    | Ordem de Serviço |
| `interfaces/api/stock/StockControllerIT.java`               | HTTP via REST-assured    | Estoque          |

> Os testes de orçamento (`findById`, `findByServiceOrderId`) ficam na camada de serviço pois não há endpoints GET de orçamento no controller.

### Executar apenas os testes de integração

```bash
./mvnw verify -DskipUnitTests
```

### Executar apenas os testes unitários

```bash
./mvnw test
```

### Executar tudo (unitários + integração)

```bash
./mvnw verify
```

### Como funciona

- O container PostgreSQL (`postgres:16`) sobe uma única vez para toda a suite via `static {}` block em `IntegrationTestBase`, sendo compartilhado entre todas as classes de teste.
- O Flyway aplica as migrations automaticamente ao iniciar o contexto Spring.
- Cada teste limpa os dados inseridos no `@AfterEach`, respeitando a ordem de foreign keys.
- Os testes de controller usam JWT real obtido via `POST /api/v1/login` antes de cada teste.

---

## Análise de cobertura de testes e vulnerabilidades com SonarQube

Este guia apresenta como configurar e executar o SonarQube localmente para analisar a cobertura de testes e possíveis vulnerabilidades da aplicação.

---

### Primeiro Acesso

#### 1. Subir o container do SonarQube

```bash
docker compose -f compose.sonar.yaml up -d
```

#### 2. Acessar a interface web do SonarQube

1. Abra o navegador:
- Acesse: http://localhost:9000

2. Faça login com usuário e senha padrão:
- Usuário: **admin**
- Senha: **admin**

#### 3. Alterar senha inicial

No primeiro acesso, o SonarQube solicitará a alteração da senha padrão.

#### 4. Ajustar permissões e opções de autenticação para análise local
1. Acesse `Administration` > `General Settings` > `Security` (ou acesse diretamente http://localhost:9000/admin/settings?category=security)
   1. No menu lateral da esquerda, clique em `Security`
   2. Desative a opção `Force user authentication`
2. Acesse `Administration` > `Global permissions` (ou acesse diretamente http://localhost:9000/admin/permissions)
   1. Localize o grupo de usuários `Anyone`
   2. Marque as opções `Execute Analysis` e `Create`

#### 5. Executar análise

No diretório raiz do projeto execute:
```bash
./mvnw clean verify sonar:sonar
```

Na primeira execução, o projeto `ofisy` será criado automaticamente no SonarQube local.

#### 6. Consultar resultado da análise

- Acesse: http://localhost:9000/
- Verifique a última análise executada
  - Para mais detalhes, pode clicar no projeto `ofisy` que será exibido uma visão geral das últimas análises executadas

## Testes de Segurança (OWASP ZAP)

### Executando os testes (Completo)

#### 1. Subir aplicação junto com o ZAP
```bash
docker compose -f compose.yaml -f compose.zap.yaml up --build
```

O processo irá:

1. Subir a aplicação
2. Aguardar o serviço ficar disponível
3. Executar o scan automatizado de segurança

### Relatórios Gerados

Os relatórios são salvos no diretório `./zap-reports/`:

- `full-report.html` - Relatório visual completo
- `full-report.json` - Útil para integração com CI/CD
- `full-report.xml` - Útil para integração com CI/CD e ferramentas externas

### Como visualizar o relatório

Abra o arquivo no navegador: `./zap-reports/full-report.html`

### Tipos de testes executados
- Análise passiva:
  - Headers de segurança
  - Cookies
  - Vazamento de informações
- Testes ativos:
  - SQL Injection
  - XSS
  - SSRF
  - Path Traversal
  - Command Injection

### Autenticação

Esta configuração executa testes de segurança automatizados com autenticação via JWT, permitindo a análise de endpoints protegidos.

- O ZAP realiza login automático via /api/v1/login
- Extrai o token JWT da resposta
- Injeta o token no header Authorization das requisições