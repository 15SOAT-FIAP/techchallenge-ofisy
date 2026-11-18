# Banco de Dados - Ofisy

> Documento de referência técnica sobre a escolha, o modelo relacional e os relacionamentos do
> banco de dados do core da Ofisy. Baseado no schema real versionado via Flyway em
> `techchallenge-ofisy` (migrations `V3` a `V30`) e na infraestrutura provisionada em
> `techchallenge-ofisy-db-infra`.
>
> Cada microsserviço tem o próprio banco ([ADR-0010](adr/0010-dividir-a-aplicacao-em-microsservicos-por-bounded-context.md)):
> orçamento e cobrança ficam no banco do `ms-ofisy-billing`, estoque no do `ms-ofisy-stock`,
> execução no do `ms-ofisy-execution` e notificações no DynamoDB do `ms-ofisy-notification`
> ([ADR-0013](adr/0013-utilizar-dynamodb-no-ms-ofisy-notification.md)). Este documento cobre só o banco do core.
>
> **Diagrama editável (draw.io)**: [`database-diagram.drawio`](resources/database-diagram.drawio)
>
> **Diagrama visual (arquivo .png):** [`database-diagram.png`](resources/database-diagram.png)

---

## 1. Escolha do banco de dados

### 1.1 Decisão

**Amazon RDS para PostgreSQL 15**, instância `db.t3.micro`, armazenamento `gp2` de 20 GB,
criptografado em repouso, em subnets privadas, sem acesso público, provisionado via Terraform
pela raiz `core/` do `techchallenge-ofisy-db-infra`, que usa o módulo compartilhado
`modules/postgresql/` ([ADR-0014](adr/0014-manter-a-infraestrutura-de-bancos-em-um-unico-repositorio.md)).

### 1.2 Justificativa formal

O domínio do core (clientes, veículos, usuários, catálogo de serviços e ordens de serviço) é
fortemente relacional: as entidades têm cardinalidade bem definida e integridade referencial
obrigatória (uma ordem de serviço *não existe* sem um veículo, um cliente e o funcionário que a
abriu).

Esse perfil de domínio orienta
a escolha para um banco relacional com garantias ACID, e não para um armazenamento
NoSQL orientado a documentos ou chave-valor. Os critérios que sustentam a escolha específica do
PostgreSQL são:

| Critério                                      | Como o PostgreSQL atende                                                                                                                                                                                                                                 |
|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Integridade referencial e transacional**    | Suporte completo a `FOREIGN KEY`, `CHECK`, `UNIQUE` e transações ACID, usados extensivamente no schema (ver seção 2) para impor regras de negócio no nível do banco, não apenas na aplicação. A mudança de status da OS e o evento do outbox são gravados na mesma transação ([MESSAGING.md](MESSAGING.md)). |
| **Consumo por múltiplos serviços/linguagens** | O banco é acessado tanto pelo core Java/Spring (`techchallenge-ofisy`, via JPA/Flyway) quanto pela Lambda em Go (`techchallenge-ofisy-auth`, via `pgx`/`sqlx`), que lê `customers` para autenticar o cliente. O protocolo e o driver do Postgres têm suporte maduro e estável nas duas linguagens. |
| **Identificadores sem coordenação**           | A função nativa `gen_random_uuid()` gera UUIDs sem uma sequência centralizada. Os ids da OS e do catálogo viajam nos eventos e nas chamadas aos microsserviços, que os guardam como referência sem conflito.                                                |
| **Constraints declarativas de estado**        | `CHECK` constraints (ex.: `service_orders.status`) tornam a máquina de estados do domínio auditável diretamente no schema, como uma segunda camada de defesa complementar à validação feita em `ServiceOrder.canTransitionTo`.                           |
| **Maturidade operacional na AWS**             | RDS gerencia patching e criptografia, reduzindo o esforço operacional de um time pequeno (Tech Challenge).                                                                                                                                               |
| **Custo/escala do projeto**                   | `db.t3.micro` atende à carga do desafio; o modelo relacional permite crescer verticalmente (instance class) e futuramente para `Multi-AZ` sem re-desenho do schema.                                                                                      |

### 1.3 Infraestrutura provisionada

```
Amazon RDS PostgreSQL 15 (db.t3.micro, gp2 20GB)
├─ identifier: ms-ofisy-core-db
├─ db_name: ms_ofisy_core_db
├─ usuário: admin_db (senha vinda da secret DB_PASSWORD)
├─ Subnets privadas (DB Subnet Group cobrindo 2 AZs, sem acesso público)
├─ Storage encryption: habilitado
├─ Backup: desabilitado (backup_retention_period = 0) e sem snapshot final, porque o
│  ambiente do AWS Academy é recriado a cada sessão
├─ Multi-AZ: desabilitado (custo do desafio). Subnet group já cobre 2 AZs, permitindo
│  habilitar Multi-AZ sem re-provisionar rede
└─ Security Group (ms-ofisy-core-db-sg): libera a porta 5432 para o CIDR da VPC e para o
   SG da Lambda de autenticação (ofisy-lambda-auth-sg, criado no techchallenge-ofisy-eks-infra)
```

Repositório responsável: [`techchallenge-ofisy-db-infra`](https://github.com/15SOAT-FIAP/techchallenge-ofisy-db-infra).
A ordem de provisionamento entre os repositórios está no README do
[`ofisy-platform`](https://github.com/15SOAT-FIAP/ofisy-platform), que sobe o ambiente inteiro.

---

## 2. Ajustes no modelo relacional

O schema evoluiu de forma incremental e versionada (Flyway, `V3` a `V30`). As migrations de
orçamento, de execução, de estoque e de notificações foram removidas quando esses domínios
saíram para o `ms-ofisy-billing`, o `ms-ofisy-execution`, o `ms-ofisy-stock` e o
`ms-ofisy-notification`, por isso a numeração tem lacunas. Os principais ajustes de modelagem, e
as razões por trás deles, estão descritos abaixo.

### 2.1 Chaves primárias como UUID

Todas as tabelas usam `UUID PRIMARY KEY DEFAULT gen_random_uuid()` em vez de inteiros
autoincrementais.

- **Motivo:** os ids do core são referenciados fora dele: o `serviceOrderId` é o identificador
  dos eventos da saga e o `message-group-id` das filas FIFO, e o `serviceCatalogId` é guardado no
  orçamento e nas execuções. Um UUID pode ser gerado antes da persistência (útil em testes e nos
  usecases da camada de aplicação) e evita vazamento de informação de volume de registros pelo ID
  (comum em PKs sequenciais expostas em APIs públicas, como `GET /service-orders/{id}/status`).

### 2.2 Aggregates isolados por UUID de referência (sem grafo de objetos entre agregados)

O código Java (Clean Architecture) não mapeia relações entre agregados diferentes com JPA. As
referências (`vehicle_id`, `customer_id`, `created_by`) são armazenadas como coluna `UUID`
simples, sem mapeamento `@ManyToOne`/navegação de objeto, e a integridade é garantida só pela
`FOREIGN KEY` do schema.

- **Motivo:** reduz o acoplamento entre agregados (Customer, Vehicle, ServiceOrder etc. evoluem
  de forma independente no código), evita *lazy loading* acidental entre agregados e mantém a
  responsabilidade de integridade referencial explicitamente no banco. Foi esse isolamento que
  permitiu extrair orçamento, estoque, execução e notificações para microsserviços sem reescrever
  os agregados que ficaram no core.

### 2.3 Exclusão lógica (soft delete) via flag `active`

`users.active` (desde `V5`) e `customers.active` (adicionado em `V27`) substituem a exclusão física
de registros.

- **Motivo:** clientes e usuários inativados continuam referenciados por ordens de serviço,
  veículos e histórico já existentes; excluir fisicamente quebraria a integridade referencial e o
  histórico de atendimento. A autenticação (tanto o login do funcionário quanto a Lambda de CPF/CNPJ)
  consulta explicitamente esse flag antes de emitir um token.

### 2.4 Máquina de estados reforçada por `CHECK` constraint

```sql
-- V11
CHECK (status IN ('RECEIVED','IN_DIAGNOSTIC','AWAITING_APPROVAL','AWAITING_EXECUTION',
                   'IN_PROGRESS','FINISHED','PAID','DELIVERED','CANCELLED'))
```

- **Motivo:** a transição de estados de `ServiceOrder` já é validada no
  domínio Java (`ServiceOrderStatus.canTransitionTo`), mas o `CHECK` no banco garante que **nenhum**
  processo (incluindo scripts manuais, jobs futuros ou bugs de aplicação) grave um valor de status
  fora do vocabulário conhecido. Com a saga, a maior parte das transições vem dos eventos dos
  microsserviços ([MESSAGING.md](MESSAGING.md#eventos-consumidos)).

### 2.5 Evolução aditiva, nunca destrutiva

`V26` (`priority` em `service_orders`), `V27` (`active` em `customers`) e `V30` (`trace_context` em
`outbox_events`) são exemplos de `ALTER TABLE ... ADD COLUMN`, com valor default e *backfill* via
`UPDATE` quando necessário, preservando os dados já existentes e permitindo deploy sem downtime.
As únicas remoções do histórico foram as das migrations dos domínios extraídos, descritas acima.

---

## 3. Diagrama ER

O diagrama entidade-relacionamento completo está disponível nos links abaixo:

**Diagrama Editável** - **[`database-diagram.drawio`](resources/database-diagram.drawio)**.

**Diagrama Visual** - **[`database-diagram.png`](resources/database-diagram.png)**.

Convenções usadas no diagrama:

| Marcação                   | Significado                                                                                         |
|----------------------------|-----------------------------------------------------------------------------------------------------|
| **`[PK]`** (negrito)       | Chave primária                                                                                      |
| *`[FK]`* (itálico)         | Chave estrangeira                                                                                   |
| <u>`[UK]`</u> (sublinhado) | Restrição `UNIQUE`                                                                                  |
| `[CHECK]`                  | Coluna protegida por `CHECK constraint` de valores válidos                                          |
| Linha cheia                | Relacionamento com `FOREIGN KEY` declarada no banco                                                 |
| Notação nas pontas         | Cardinalidade "pé de galinha" (crow's foot): um traço = "1", garfo = "N", círculo = "0..1" opcional |

As 5 tabelas de negócio do schema estão organizadas em 2 grupos visuais no diagrama. As tabelas de
mensageria (`outbox_events` e `processed_events`, criadas em `V29` e `V28`, com a coluna
`trace_context` do outbox adicionada em `V30`) ficam fora do diagrama e estão descritas no
[guia de mensageria](MESSAGING.md).

- **Cadastro**: `users`, `customers`, `vehicles`
- **Fluxo da Ordem de Serviço**: `service_orders`, `services_catalog`

---

## 4. Explicação sobre os relacionamentos

| #  | Relacionamento                             | Cardinalidade     | Regra de negócio                                                                                                                                                                                               |
|----|--------------------------------------------|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | `customers` → `vehicles`                   | 1 : N             | Um cliente pode ter vários veículos cadastrados (`vehicles.customer_id`); um veículo pertence a exatamente um cliente.                                                                                         |
| 2  | `customers` → `service_orders`             | 1 : N             | Toda ordem de serviço referencia o cliente dono do veículo no momento da abertura. Mantido mesmo que o veículo troque de dono depois, preservando o histórico correto de quem contratou aquele atendimento.    |
| 3  | `vehicles` → `service_orders`              | 1 : N             | Um veículo pode ter várias ordens de serviço ao longo do tempo (histórico de manutenções).                                                                                                                     |
| 4  | `users` → `service_orders` (`created_by`)  | 1 : N             | Toda OS registra qual funcionário (ADMIN/ATTENDANT) a abriu, para rastreabilidade/auditoria.                                                                                                                   |
| 5  | `service_orders` → microsserviços          | 1 : N (sem FK)    | O orçamento e a cobrança (`ms-ofisy-billing`), as execuções (`ms-ofisy-execution`), as movimentações de estoque (`ms-ofisy-stock`) e as notificações (`ms-ofisy-notification`) guardam o `serviceOrderId` como referência lógica, nos próprios bancos. |
| 6  | `services_catalog` → microsserviços        | 1 : N (sem FK)    | Os itens de serviço do orçamento (`ms-ofisy-billing`) e as execuções (`ms-ofisy-execution`) guardam o `serviceCatalogId`. O billing lê o nome e o preço pela API do core (`GET /api/v1/services-catalog/{id}`) e guarda uma cópia no item. |

---

## 5. Referências

- Migrations Flyway: `techchallenge-ofisy/src/main/resources/db/migration/V3__*.sql` … `V30__*.sql`
- Infraestrutura RDS: `techchallenge-ofisy-db-infra/core/main.tf` e `techchallenge-ofisy-db-infra/modules/postgresql/rds.tf`