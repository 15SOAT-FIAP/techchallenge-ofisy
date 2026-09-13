# Banco de Dados - Ofisy

> Documento de referência técnica sobre a escolha, o modelo relacional e os relacionamentos do
> banco de dados da plataforma Ofisy. Baseado no schema real versionado via Flyway em
> `techchallenge-ofisy` (migrations `V1` a `V27`) e na infraestrutura provisionada em
> `techchallenge-ofisy-rds-infra`.
>
> 
> **Diagrama editável (draw.io)**: [`database-diagram.drawio`](resources/database-diagram.drawio)
> 
> **Diagrama visual (arquivo .png):** [`database-diagram.png`](resources/database-diagram.png)

---

## 1. Escolha do banco de dados

### 1.1 Decisão

**Amazon RDS para PostgreSQL 15**, instância `db.t3.micro`, armazenamento `gp2` de 20 GB,
criptografado em repouso, em subnets privadas, sem acesso público, provisionado via Terraform em
`techchallenge-ofisy-rds-infra/infra/rds.tf`.

### 1.2 Justificativa formal

O domínio do Ofisy (clientes, veículos, ordens de serviço, orçamentos, execuções, estoque) é
fortemente relacional: as entidades têm cardinalidade bem definida, integridade referencial
obrigatória (uma ordem de serviço *não existe* sem um veículo e um cliente válidos) e regras de
consistência transacional (um orçamento só pode ser aprovado se existir uma OS associada).

A finalização automática da OS depende de uma contagem consistente de execuções concluídas). Esse perfil de domínio orienta
a escolha para um banco relacional com garantias ACID, e não para um armazenamento
NoSQL orientado a documentos ou chave-valor. Os critérios que sustentam a escolha específica do
PostgreSQL são:

| Critério | Como o PostgreSQL atende                                                                                                                                                                                                                                                                                                      |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Integridade referencial e transacional** | Suporte completo a `FOREIGN KEY`, `CHECK`, `UNIQUE` e transações ACID, usados extensivamente no schema (ver seção 2) para impor regras de negócio no nível do banco, não apenas na aplicação.                                                                                                                                 |
| **Consumo por múltiplos serviços/linguagens** | O banco é acessado tanto pelo monolito Java/Spring (`techchallenge-ofisy`, via JPA/Flyway) quanto pela Lambda em Go (`techchallenge-ofisy-auth`, via `pgx`/`sqlx`). O protocolo e o driver do Postgres têm suporte maduro e estável nas duas linguagens.                                                                      |
| **Geração de identificadores distribuída** | A função nativa `gen_random_uuid()` (extensão `pgcrypto`/`uuid-ossp` disponível por padrão no RDS Postgres) permite que dois serviços diferentes insiram registros (ex.: `customers`) sem depender de uma sequência autoincremental centralizada. |
| **Constraints declarativas de estado** | `CHECK` constraints (ex.: `service_orders.status`, `service_order_executions.status`) tornam a máquina de estados do domínio auditável diretamente no schema, como uma segunda camada de defesa complementar à validação feita em `ServiceOrder.canTransitionTo`.                                                             |
| **Maturidade operacional na AWS** | RDS gerencia patching, backups automáticos (retenção de 7 dias) e criptografia, reduzindo o esforço operacional de um time pequeno (Tech Challenge).                                                                                                                                                                          |
| **Custo/escala do projeto** | `db.t3.micro` atende à carga do desafio; o modelo relacional permite crescer verticalmente (instance class) e futuramente para `Multi-AZ` sem re-desenho do schema.                                                                                                                                                           |

### 1.3 Infraestrutura provisionada

```
Amazon RDS PostgreSQL 15 (db.t3.micro, gp2 20GB)
├─ db_name: ofisydb
├─ Subnets privadas (DB Subnet Group cobrindo 2 AZs, sem acesso público)
├─ Storage encryption: habilitado
├─ Backup retention: 7 dias (janela 03:00–04:00 UTC)
├─ Maintenance window: domingo 04:00–05:00 UTC
├─ Multi-AZ: desabilitado no ambiente atual (custo do desafio). Subnet group já
│  cobre 2 AZs, permitindo habilitar Multi-AZ sem re-provisionar rede
└─ Security Group: libera porta 5432 apenas para os SGs do EKS (aplicação) e da
   Lambda de autenticação (ver techchallenge-ofisy-eks-infra)
```

Repositório responsável: [`techchallenge-ofisy-rds-infra`](https://github.com/15SOAT-FIAP/techchallenge-ofisy-rds-infra).
A ordem de provisionamento entre repositórios (rede → auth Lambda no ECR → RDS → Lambda authorizer
→ aplicação no EKS) está documentada no README daquele repositório.

---

## 2. Ajustes no modelo relacional

O schema evoluiu de forma incremental e versionada (Flyway, `V1` a `V27`), começando pelo domínio
de estoque e chegando a um modelo completo de ordens de serviço, orçamentos e execuções. Os
principais ajustes de modelagem, e as razões por trás deles, estão descritos abaixo.

### 2.1 Chaves primárias como UUID

Todas as tabelas usam `UUID PRIMARY KEY DEFAULT gen_random_uuid()` em vez de inteiros
autoincrementais.

- **Motivo:** evita coordenação de um único gerador de sequência entre os dois serviços que
  escrevem no banco (o backend Java e a Lambda Go de autenticação), simplifica a geração de IDs
  antes da persistência (útil em testes e nos usecases da camada de aplicação) e evita vazamento de
  informação de volume de registros pelo ID (comum em PKs sequenciais expostas em APIs públicas,
  como `GET /service-orders/{id}/status`).

### 2.2 Aggregates isolados por UUID de referência (sem grafo de objetos entre agregados)

O código Java (Clean Architecture) mapeia com JPA apenas relações internas a um mesmo
agregado, por exemplo, `Quote` tem `@OneToMany` para `QuoteStockItem` e `QuoteServiceItem`
(cascade `ALL`, `orphanRemoval`), pois esses itens não existem fora do orçamento que os contém.
Já as referências entre agregados diferentes (`vehicle_id`, `customer_id`, `created_by`,
`stock_id`, `service_catalog_id` etc.) são armazenadas como coluna `UUID` simples, sem
mapeamento `@ManyToOne`/navegação de objeto, a integridade é garantida só pela `FOREIGN KEY` do
schema.

- **Motivo:** reduz o acoplamento entre bounded contexts (Customer, Vehicle, ServiceOrder, Stock,
  Quote etc. evoluem de forma independente no código), evita *lazy loading* acidental entre
  agregados e mantém a responsabilidade de integridade referencial explicitamente no banco.

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
                   'IN_PROGRESS','FINISHED','DELIVERED','CANCELLED'))
-- V13
CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','CANCELLED'))
```

- **Motivo:** a transição de estados de `ServiceOrder` e `ServiceOrderExecution` já é validada no
  domínio Java (`ServiceOrderStatus.canTransitionTo`), mas o `CHECK` no banco garante que **nenhum**
  processo (incluindo scripts manuais, jobs futuros ou bugs de aplicação) grave um valor de status
  fora do vocabulário conhecido.

### 2.5 Evolução aditiva, nunca destrutiva

`V26` (`priority` em `service_orders`) e `V27` (`active` em `customers`) são exemplos de
`ALTER TABLE ... ADD COLUMN` com valor default e *backfill* via `UPDATE`, preservando os dados já
existentes e permitindo deploy sem downtime. Nenhuma migration do histórico remove ou renomeia
coluna/tabela já existente.

---

## 3. Diagrama ER

O diagrama entidade-relacionamento completo está disponível nos links abaixo:

**Diagrama Editável** - **[`database-diagram.drawio`](resources/database-diagram.drawio)**.

**Diagrama Visual** - **[`database-diagram.png`](resources/database-diagram.png)**.

Convenções usadas no diagrama:

| Marcação | Significado                                                                                         |
|---|-----------------------------------------------------------------------------------------------------|
| **`[PK]`** (negrito) | Chave primária                                                                                      |
| *`[FK]`* (itálico) | Chave estrangeira                                                                                   |
| <u>`[UK]`</u> (sublinhado) | Restrição `UNIQUE`                                                                                  |
| ***<u>`[FK/UK]`</u>*** | Coluna que é FK **e** UNIQUE ao mesmo tempo (relacionamento 1:1)                                    |
| `[CHECK]` | Coluna protegida por `CHECK constraint` de valores válidos                                          |
| Linha cheia | Relacionamento com `FOREIGN KEY` declarada no banco                                                 |
| Linha tracejada vermelha | Coluna de referência opcional (nullable) e, no caso de `notifications.quote_id`, sem FK declarada   |
| Notação nas pontas | Cardinalidade "pé de galinha" (crow's foot): um traço = "1", garfo = "N", círculo = "0..1" opcional |

As 12 tabelas do schema estão organizadas em 5 grupos visuais no diagrama:

- **Cadastro**: `users`, `customers`, `vehicles`
- **Fluxo da Ordem de Serviço**: `service_orders`, `service_order_executions`, `services_catalog`
- **Orçamento**: `quotes`, `quote_stock_items`, `quote_service_items`
- **Estoque**: `stocks`, `stock_movements`
- **Notificações**: `notifications`
---

## 4. Explicação sobre os relacionamentos

| # | Relacionamento | Cardinalidade | Regra de negócio                                                                                                                                                                                                                                 |
|---|---|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | `customers` → `vehicles` | 1 : N | Um cliente pode ter vários veículos cadastrados (`vehicles.customer_id`); um veículo pertence a exatamente um cliente.                                                                                                                           |
| 2 | `customers` → `service_orders` | 1 : N | Toda ordem de serviço referencia o cliente dono do veículo no momento da abertura. Mantido mesmo que o veículo troque de dono depois, preservando o histórico correto de quem contratou aquele atendimento.                                      |
| 3 | `vehicles` → `service_orders` | 1 : N | Um veículo pode ter várias ordens de serviço ao longo do tempo (histórico de manutenções).                                                                                                                                                       |
| 4 | `users` → `service_orders` (`created_by`) | 1 : N | Toda OS registra qual funcionário (ADMIN/ATTENDANT) a abriu, para rastreabilidade/auditoria.                                                                                                                                                     |
| 5 | `service_orders` → `quotes` | 1 : 0..1 (hoje) | Uma OS possui no máximo um orçamento (`quotes.service_order_id UNIQUE`): `CreateQuoteService` bloqueia um segundo orçamento para a mesma OS (`QuoteAlreadyExistsException`).                                                                       |
| 6 | `service_orders` → `service_order_executions` | 1 : N | Cada serviço incluído no orçamento gera uma "execução" própria (`ServiceOrderExecution`), permitindo acompanhar individualmente o andamento (`PENDING → IN_PROGRESS → COMPLETED/CANCELLED`) de cada item de serviço dentro da mesma OS.          |
| 7 | `services_catalog` → `service_order_executions` | 1 : N | O catálogo de serviços (nome, descrição, preço) é reaproveitado por várias execuções, em OSs diferentes.                                                                                                                                         |
| 8 | `quotes` → `quote_stock_items` | 1 : N | Itens de peças/estoque incluídos no orçamento, com preço e quantidade "congelados" no momento da cotação (mesmo que o preço do estoque mude depois).                                                                                             |
| 9 | `quotes` → `quote_service_items` | 1 : N | Itens de serviço incluídos no orçamento, cada um associado a uma `service_order_execution` específica criada junto com o orçamento.                                                                                                              |
| 10 | `stocks` → `quote_stock_items` | 1 : N | Uma peça do estoque pode aparecer em vários orçamentos ao longo do tempo; a criação do item já consome (reserva) a quantidade do estoque (`ConsumeStockUseCase`).                                                                                |
| 11 | `service_order_executions` → `quote_service_items` | 1 : 0..1 | Cada execução de serviço é criada exclusivamente para um item de orçamento específico (par único `quote_id + service_order_executions_id`), por isso a cardinalidade é efetivamente 1 para 1 na prática, embora o schema modele como FK simples. |
| 12 | `stocks` → `stock_movements` | 1 : N | Toda entrada/saída de estoque gera um registro imutável de movimentação, com quantidade anterior e nova.                                                                                                                                         |
| 13 | `stocks` → `notifications` | 0..1 : N | Notificação de estoque baixo (`type = LOW_STOCK`) referencia opcionalmente o item de estoque que a originou; `stock_id` é nullable desde `V14` para permitir notificações de outros tipos sem essa referência.                                   |
| 14 | `quotes` → `notifications` | 0..1 : N (sem FK) | Notificação de orçamento pendente de aprovação (`type = QUOTE`) referencia opcionalmente o orçamento.                                                                                                                                            |

---

## 5. Referências

- Migrations Flyway: `techchallenge-ofisy/src/main/resources/db/migration/V1__*.sql` … `V27__*.sql`
- Infraestrutura RDS: `techchallenge-ofisy-rds-infra/infra/rds.tf`
