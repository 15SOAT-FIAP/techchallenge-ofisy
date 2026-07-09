# Diagrama de Componentes - Ofisy

Diagrama de componentes do
**container Backend** da aplicação Ofisy (Spring Boot / Java 21), usando Clean Architecture.

São 10 agregados de negócio: Customer, Vehicle, Stock, StockMovement, ServiceCatalog,
ServiceOrder, ServiceOrderExecution, Quote, User e Notification. Todos seguem o mesmo caminho de
camadas `Controller → Use Case → Gateway`. Desenhar os 10 separadamente só polui o diagrama, então
eles aparecem agrupados em um bloco só. À parte disso, dois mecanismos têm lógica própria e
merecem caixa dedicada: a autenticação via JWT e as notificações internas.

```mermaid
%%{init: {'theme':'neutral', 'flowchart': {'curve':'basis'}}}%%
flowchart TB
    %% ---------- Actors / external ----------
    Client(["<b>API Client</b><br/><i>[Software System]</i><br/>Consumidor REST genérico<br/>(Postman, curl, integrações)."])

    subgraph Backend["<b>Backend</b> &nbsp; [Container: Spring Boot / Java 21]"]
        direction TB

        %% ---------- Interface layer ----------
        subgraph InterfaceLayer["Interface Adapters: Controllers [REST]"]
            direction LR
            LoginCtrl["<b>Login Controller</b><br/><i>[Component: Spring MVC]</i><br/>POST /api/v1/login:<br/>autentica e emite JWT."]
            BizCtrls["<b>Business Controllers</b><br/><i>[Component: Spring MVC]</i><br/>Customer, Vehicle, Stock,<br/>StockMovement, ServiceCatalog,<br/>ServiceOrder, Execution, Quote,<br/>User, Notification."]
        end

        %% ---------- Application layer ----------
        subgraph AppLayer["Application: Use Cases (orquestração)"]
            direction LR
            LoginUC["<b>Login Use Case</b><br/><i>[Component: Spring Bean]</i><br/>Valida credenciais e<br/>solicita geração de token."]
            BizUC["<b>Business Use Cases</b><br/><i>[Component: Spring Bean]</i><br/>Um caso de uso por operação<br/>(create, list, approve, cancel…)<br/>coordenando as entidades<br/>de domínio."]
            NotifUC["<b>Notification Use Cases</b><br/><i>[Component: Spring Bean]</i><br/>Cria e consulta notificações<br/>internas (orçamento gerado,<br/>estoque baixo)."]
        end

        %% ---------- Domain ----------
        Domain["<b>Domain Model</b><br/><i>[Component: POJO / DDD]</i><br/>Entidades ricas, value objects e<br/>regras de negócio por agregado.<br/>Define as interfaces de repositório."]

        %% ---------- Cross-cutting security ----------
        subgraph SecLayer["Segurança"]
            direction TB
            JwtFilter["<b>JWT Auth Filter</b><br/><i>[Component: Spring Security Filter]</i><br/>Intercepta cada request, valida o<br/>token e popula o SecurityContext."]
            JwtService["<b>JWT Service / Token Generator</b><br/><i>[Component: Spring Bean]</i><br/>Gera e valida JWTs (jjwt)."]
        end

        %% ---------- Interface adapters: gateways ----------
        Gateways["<b>Gateways (Repository Impl)</b><br/><i>[Component: Spring Bean]</i><br/>Implementam as interfaces de<br/>repositório do domínio; mapeiam<br/>entidade de domínio ↔ entidade JPA."]

        JpaRepos["<b>Spring Data JPA Repositories</b><br/><i>[Component: Spring Data]</i><br/>Persistência ORM sobre o banco."]
    end

    %% ---------- Infra containers ----------
    Db[("<b>Database</b><br/><i>[Container: PostgreSQL 16]</i><br/>Dados de OS, orçamentos,<br/>estoque, usuários,<br/>notificações etc.")]

    %% Swagger declarado aqui (fora do topo) para não cruzar com as setas do API Client
    Swagger(["<b>Swagger UI</b><br/><i>[Software System]</i><br/>Documentação e testes<br/>manuais das rotas"])

    %% ================= Relationships =================
    Client -->|"Autentica-se via<br/>[JSON/HTTPS]"| LoginCtrl
    Client -->|"Consome recursos de negócio<br/>com Bearer JWT [JSON/HTTPS]"| BizCtrls

    JwtFilter -.->|"Autentica requests<br/>antes dos controllers"| BizCtrls
    JwtFilter -->|"Valida token usando"| JwtService

    LoginCtrl --> LoginUC
    BizCtrls --> BizUC

    LoginUC -->|"Gera token usando"| JwtService
    LoginUC --> Domain
    BizUC --> Domain
    BizUC -->|"Dispara notificações<br/>(orçamento / estoque baixo)"| NotifUC
    NotifUC --> Domain

    LoginUC --> Gateways
    BizUC --> Gateways
    NotifUC --> Gateways
    Gateways -->|"usa as interfaces<br/>definidas no"| Domain

    Gateways --> JpaRepos
    JpaRepos -->|"Lê e escreve em<br/>[JDBC / SQL]"| Db

    Swagger -.->|"Chama os endpoints<br/>para teste manual<br/>[JSON/HTTPS]"| BizCtrls

    %% ================= Styling =================
    classDef comp fill:#1168bd,stroke:#0b4884,color:#ffffff;
    classDef ext fill:#999999,stroke:#6b6b6b,color:#ffffff;
    classDef db fill:#438dd5,stroke:#2e6295,color:#ffffff;

    class LoginCtrl,BizCtrls,LoginUC,BizUC,NotifUC,Domain,JwtFilter,JwtService,Gateways,JpaRepos comp;
    class Client,Swagger ext;
    class Db db;
```

## Componentes

| Componente                        | Tecnologia             | Responsabilidade                                                                                                                                                                                                                     |
|-----------------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Login Controller**              | Spring MVC             | Endpoint de autenticação (`POST /api/v1/login`); delega ao caso de uso de login.                                                                                                                                                     |
| **Business Controllers**          | Spring MVC             | Um controller por agregado (Customer, Vehicle, Stock, StockMovement, ServiceCatalog, ServiceOrder, ServiceOrderExecution, Quote, User, Notification). Expõem as rotas REST e delegam a casos de uso. Protegidos por `@PreAuthorize`. |
| **Login Use Case**                | Spring Bean            | Valida credenciais (senha via `PasswordEncoder`) e solicita a emissão do token.                                                                                                                                                      |
| **Business Use Cases**            | Spring Bean            | Um caso de uso por operação (create, list, approve, cancel, startDiagnostic…). Coordenam as entidades de domínio e os gateways.                                                                                                      |
| **Notification Use Cases**        | Spring Bean            | Criam e consultam notificações internas. Outros casos de uso os chamam direto, no mesmo processo, quando geram um orçamento ou detectam estoque baixo. As notificações ficam no banco; nesta fase não sai e-mail nem SMS.           |
| **Domain Model**                  | POJO / DDD             | Entidades ricas, value objects (ex.: `Email`, `CpfCnpj`, `LicensePlate`) e regras de negócio. Define as interfaces de repositório.                                                                                                   |
| **JWT Auth Filter**               | Spring Security Filter | Intercepta cada requisição, valida o Bearer token e popula o `SecurityContext`.                                                                                                                                                      |
| **JWT Service / Token Generator** | Spring Bean (jjwt)     | Geração e validação dos tokens JWT.                                                                                                                                                                                                  |
| **Gateways (Repository Impl)**    | Spring Bean            | Implementam as interfaces de repositório do domínio e mapeiam domínio ↔ entidade JPA.                                                                                                                                                |
| **Spring Data JPA Repositories**  | Spring Data            | Persistência ORM.                                                                                                                                                                                                                    |

## Containers relacionados

| Container            | Tecnologia        | Papel                                                                         |
|----------------------|-------------------|-------------------------------------------------------------------------------|
| **Database**         | PostgreSQL 16     | Persistência relacional. Schema versionado via Flyway (ver `docs/FLYWAY.md`). |
| **API Client**       | (n/a)             | Consumidor REST genérico (Postman, curl, integrações).                        |
| **Swagger UI**       | springdoc-openapi | Documentação interativa das rotas.                                            |