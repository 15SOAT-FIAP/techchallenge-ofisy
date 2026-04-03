# Ofisy 

FIAP 15SOAT15 - Tech Challenge Fase 1 - Grupo 38

Sistema de gestão de oficina automotiva desenvolvido com Spring Boot e DDD.

---
## Estrutura do Projeto

Visto a solicitação da criação nesta primeira etapa do projeto de um monolito, optamos pela organização do projeto como um **Monolito Modular**.

A utilização do Monolito Modular, além de permitir a criação da aplicação de maneira mais organizada, funcionando sob uma mesma base de dados, facilita a transição desse projeto para uma arquitetura de microsserviços posteriormente, algo que será exigido nas próximas fases do projeto.

A estruturação das classes e entidades criadas seguirá o modelo de **arquitetura em camadas**:

```
br.com.ofisy
├── config/       # Configurações transversais (segurança, Swagger, etc.)
├── controller/   # Endpoints REST
├── service/      # Orquestração de fluxos que envolvem mais de uma entidade
├── repository/   # Acesso ao banco de dados via Spring Data JPA
├── model/        # Modelos ricos — entidades com regras do próprio subdomínio
├── dto/          # Objetos de transferência de dados (request/response)
└── mapper/       # Conversores entre entidades e DTOs
```

---

## Stack do Projeto

A combinação dessas tecnologias permite a construção de um backend robusto e de fácil manutenção, a execução local do ambiente de desenvolvimento de forma facilitada para o time e para avaliação posterior do projeto e a futura evolução da aplicação conforme as próximas fases do projeto.

| Tecnologia | Versão | Justificativa |
|---|---|---|
| Java | 21 (LTS) | Linguagem conhecida e utilizada por todos do grupo. Maturidade, robustez e suporte da comunidade. O JDK 21 oferece melhorias de desempenho e segurança, garantindo estabilidade e suporte de longo prazo, além de compatibilidade com a versão do Spring Boot escolhida. |
| Spring Boot | 3.5.x | Totalmente compatível com Java 21. Inclui melhorias de performance, atualizações do Spring Framework 6 e suporte a observabilidade e containers. O uso de dependências como o Spring Data JPA facilita o mapeamento entre o domínio da aplicação e a estrutura de dados. |
| PostgreSQL | 16 | Banco de dados relacional escolhido devido à natureza monolítica da aplicação e à estrutura de entidades com forte relacionamento entre si. O modelo relacional facilita a representação dessas relações por meio de chaves estrangeiras, garantindo integridade entre os dados. Solução amplamente utilizada em sistemas corporativos, com versão estável e melhorias de desempenho, paralelismo e otimização de consultas. |
| Maven | 3.9.x | Escolhido como ferramenta de gerenciamento de dependências e build do projeto pela facilidade de utilização e praticidade. |
| Docker | — | Necessário para containerização da aplicação e de seus serviços dependentes, garantindo um ambiente de execução padronizado e isolado, juntamente com o banco de dados através do uso do Docker Compose. Trata-se também de uma exigência para a entrega do projeto. |

---

## Dependências

| Dependência                                                   | Descrição                                                                                                                                                                                       | Artefato                                |
|---------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------|
| Spring Web	                                                   | Dependência para criação de APIs RESTful, incluindo o servidor web (Tomcat), mapeamento de rotas e controllers	                                                                                         | `spring-boot-starter-web`               |
| Spring Data JPA	                                              | Dependência para gerenciar a comunicação com o banco de dados. Além de acelerar o desenvolvimento e reduzir o código boilerplate, permite a utilização de objetos ORM dentre outras facilidades | 	`spring-boot-starter-data-jpa`         |
| Spring Security	                                              | Dependência para tratar autenticação e autorização da aplicação                                                                                                                                 | 	`spring-boot-starter-security`         |
| Spring Security JWT                                           | Dependência para tratar autenticação e autorização da aplicação. Exigência de utilização de JWT para entrega do projeto em APIs administrativas.                                                | 	`jjwt-api`, `jjwt-impl`,`jjwt-jackson` |
| OpenAPI / Swagger	                                            | Dependência para documentação e exposição das APIs/rotas criadas para o projeto via Swagger nesta fase 1.	                                                                                      | `springdoc-openapi-starter-webmvc-ui`   |
| Spring Validation | 	Dependência para validação de DTOs e beans	                                                                                                                                                    | `spring-boot-starter-validation`        |
|Postgre	| Dependência para o banco de dados PostgreSQL                                                                                                                                                    | 	`postgresql`                           |
|Docker Compose	| Dependência para permitir utilização de docker compose para aplicação                                                                                                                           | 	`spring-boot-docker-compose`           |
|Lombok |	Dependência para permitir redução de código boilerplate através de anotações, gerando automaticamente getters, setters, construtores etc.	| `lombok`                                |
|Spring Actuator|	Dependência utilizada para obter endpoints de monitoramento e observabilidade da aplicação, como verificação de saúde (/actuator/health), métricas entre outras informações	| `spring-boot-starter-actuator`            |
|Spring Boot Test|	Dependência para escrita de testes automatizados, incluindo suporte a testes unitários e de integração com JUnit e Mockito	| `spring-boot-starter-test`                |
|Spring Security Test|	Dependência para testes de endpoints protegidos, permitindo simular usuários autenticados e verificar comportamentos de segurança nos testes	| `spring-security-test`                   |
---

## Arquivos de Configuração

- `application.yml` — configurações para subir o servidor em ambiente local, sendo banco via Docker e a aplicação na IDE;
- `application-docker.yml` — configurações para subir o ambiente completo via Docker;
- `pom.xml` — dependências do projeto gerenciadas via Maven;
- `Dockerfile` — instruções para o Docker buildar a aplicação;
- `compose.yaml` — orquestração dos containers da aplicação e do banco, permitindo subir o ambiente completo em qualquer lugar, por qualquer pessoa. Segue a nomenclatura mais recente para esse tipo de arquivo;
- `compose.db.yaml` — configurações para subir apenas o container do banco para desenvolvimento local;
- `.dockerignore` — instruções para que o Docker ignore determinados arquivos e pastas durante o build, reduzindo o tamanho da imagem gerada;
- `.gitignore` — instruções para que o Git ignore determinados tipos, pastas e denominações de arquivos;
- `.env` — variáveis sensíveis utilizadas no projeto. Este arquivo **não é versionado**;
- `.env.example` — arquivo de exemplo do `.env` para criação do ambiente local.

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

O arquivo `.env.example` já contém os valores prontos para um novo ambiente ou avaliação. Para efetuar a cópia, execute o seguinte comando:

```bash
cp env.example .env
```

> O `.env.example` possui credenciais funcionais para o ambiente de desenvolvimento e avaliação.

---

## Subindo a aplicação

### Opção 1 — Ambiente completo via Docker

Para efetuar a subida do ambiente completo, com banco e aplicação juntos, rode o seguinte comando:

```bash
docker compose up --build
```

### Opção 2 — Desenvolvimento local (banco no Docker + aplicação na IDE)

**Passo 1:** Suba primeiro o banco via Docker utilzando o comando abaixo:

```bash
docker compose -f compose.db.yaml up -d
```

**Passo 2:** Execute a subida da aplicação:

```bash
./mvnw spring-boot:run
```

> A aplicação usará o perfil `dev` por padrão, conectando ao banco em `localhost:5432`.

---

## Acesso a aplicação e Swagger

- A API estará disponível em `http://localhost:8080`.
- Um controller de teste (Hello World) está disponível em `http://localhost:8080/api/teste/hello`
- A documentação Swagger estará disponível em `http://localhost:8080/swagger-ui.html`.

---

## Variáveis de ambiente

| Variável            | Descrição                        |
|---------------------|----------------------------------|
| `POSTGRES_DB`       | Nome do banco de dados           |
| `POSTGRES_USER`     | Usuário do banco                 |
| `POSTGRES_PASSWORD` | Senha do banco                   |
| `JWT_SECRET`        | Secret para assinatura do token JWT |

---

## Observações de segurança

- O arquivo `.env` está no `.gitignore` e **não deve ser versionado**
- As instruções para criação do `.env` estão no corpo deste documento.

---

## Documentação Adicional
- **[Guia de Testes](docs/TESTING.md)** - Instruções para executar análises de cobertura de testes e segurança
