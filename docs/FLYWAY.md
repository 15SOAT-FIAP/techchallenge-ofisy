# Guia de Migrations Flyway

Este projeto usa **Flyway** para versionar e aplicar mudanças no banco de dados de forma automática.

---

## Onde o Flyway está configurado

### `src/main/resources/application.yml`

- `spring.flyway.enabled: true`
- `spring.flyway.locations: classpath:db/migration`
- `spring.jpa.hibernate.ddl-auto: validate`

Isso significa que:

- o Flyway procura scripts SQL dentro de `src/main/resources/db/migration`;
- as tabelas precisam bater com o modelo esperado pela aplicação;
- o Hibernate não cria ou altera tabelas automaticamente, apenas valida.

### `src/main/java/br/com/ofisy/infrastructure/config/db/FlywayConfig.java`

A aplicação também possui uma configuração explícita do Flyway com `@Bean(initMethod = "migrate")`, então as migrations são executadas no startup.

---

## Estrutura das migrations

As migrations devem ficar em:

```text
src/main/resources/db/migration
```

No projeto já existem estes exemplos:

- `V1__create_stocks_table.sql`
- `V2__create_notifications_table.sql`

---

## Convenção de nomeação

Use sempre o padrão:

```text
V<numero_da_versao>__<descricao_da_migration>.sql
```

Exemplos:

- `V3__add_customer_table.sql`
- `V4__create_indexes_for_stocks.sql`

### Regras importantes

- a versão deve ser sequencial;
- o nome deve descrever claramente a mudança;
- cada migration deve representar uma alteração pequena e objetiva;
- uma migration aplicada no banco não deve ser editada depois de publicada, a menos que o ambiente ainda não tenha sido compartilhado.

---

## Como criar uma nova migration

1. Identifique a próxima versão disponível.
    - Se o último arquivo for `V2__...`, crie `V3__...`.

2. Crie um novo arquivo em `src/main/resources/db/migration`.

3. Escreva o SQL necessário.

### Exemplo

Crie a migration com uma instrução `CREATE TABLE` para a nova tabela `customers` e salve o arquivo como `V3__create_customers_table.sql`.

---

## Como executar as migrations

As migrations rodam automaticamente quando a aplicação sobe.

### Desenvolvimento local

```bash
./mvnw spring-boot:run
```

No Windows, você também pode usar:

```powershell
.\mvnw.cmd spring-boot:run
```

### Ambiente com Docker

```bash
docker compose up --build
```

### O que observar no startup

- o Flyway executa as migrations pendentes;
- se o banco já estiver atualizado, nada novo será aplicado;
- se houver diferença entre entidades e banco, o `ddl-auto: validate` pode acusar erro.

---

## Boas práticas

- não crie migrations diretamente no banco;
- prefira scripts SQL pequenos e fáceis de revisar;
- sempre pense na ordem de execução;
- coloque a criação de tabelas antes de criar chaves estrangeiras;
- para mudanças grandes, divida em migrations menores.

---

## Fluxo recomendado

1. Criar o arquivo SQL em `db/migration`.
2. Subir a aplicação localmente.
3. Verificar se o Flyway aplicou a migration sem erros.
4. Confirmar que a aplicação iniciou normalmente.
5. Commitar a migration junto com a alteração de código que depende dela.