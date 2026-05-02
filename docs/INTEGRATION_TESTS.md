# Guia de Testes de Integração

Este guia apresenta a estrutura dos testes integrados criados e como executá-los de forma simples via script.

## Estrutura dos Testes

```
src/test/java/br/com/ofisy/integration/
├── IntegrationTestBase.java              ← Classe base com configuração
├── ServiceOrderIntegrationTest.java      ← Testes de OS e Orçamento (fluxo completo)
└── StockIntegrationTest.java             ← Testes do módulo de Estoque e Notificações
```
---

## Configuração

### Banco de Dados de Teste

Os testes de integração utilizam um banco PostgreSQL de testes, separado do banco de desenvolvimento, para não interferir nos dados reais.

O arquivo `compose.test.yaml` na raiz do projeto define o container do banco de testes na porta `5433`.

### Profile de Teste

As configurações do ambiente de teste estão em `src/test/resources/application-test.yml`, que aponta para o banco de testes na porta `5433`.

### Isolamento dos Dados

A classe `IntegrationTestBase` é anotada com `@Transactional`, o que garante rollback automático após cada teste. Dessa forma os dados criados em um teste não interferem nos demais.

---

## Como Executar

Execute o script na raiz do projeto:

```bash
chmod +x run-integration-tests.sh
./run-integration-tests.sh
```

Este script automaticamente:
1. Sobe o container do banco de testes (`postgres-test-db` na porta `5433`)
2. Executa os testes de integração criados
3. Derruba o container ao finalizar

### Executando pelo IntelliJ

Para rodar os testes de integração diretamente pelo IntelliJ:

1. Suba o banco de testes manualmente utilizando o comando abaixo:
```bash
docker compose -f compose.test.yaml up -d
```

2. Execute individualmente a classe de testes integrados desejada pelo IntelliJ

3. Ao finalizar, derrube o contâiner criado para o banco:
```bash
docker compose -f compose.test.yaml down
```

---

## Módulos Cobertos

### ServiceOrderIntegrationTest

Cobre os fluxos de **Ordem de Serviço** e **Orçamento** em conjunto, refletindo o modelo de negócio onde o orçamento só é criado via OS.

#### Ordem de Serviço

| Cenário | Descrição | Exception esperada |
|---|---|---|
| Criar OS | Verifica criação com status `RECEIVED` | — |
| Veículo não pertence ao cliente | — | `VehicleNotOwnedByCustomerException` |
| Cliente não encontrado | — | `CustomerNotFoundException` |
| Veículo não encontrado | — | `VehicleNotFoundException` |
| Iniciar diagnóstico | Verifica mudança para `IN_DIAGNOSTIC` | — |
| Diagnóstico em OS não encontrada | — | `ServiceOrderNotFoundException` |
| Transição de status inválida | — | `InvalidServiceOrderTransitionException` |
| Cancelar OS | Verifica mudança para `CANCELLED` | — |
| Cancelar OS não encontrada | — | `ServiceOrderNotFoundException` |
| Consultar status | Verifica retorno do status atual | — |
| Status de OS não encontrada | — | `ServiceOrderNotFoundException` |
| Listar OS recebidas | Verifica listagem paginada | — |
| Entregar ao cliente com status inválido | — | `InvalidServiceOrderTransitionException` |

#### Orçamento (via Ordem de Serviço)

| Cenário                                             | Descrição                                                    | Exception esperada |
|-----------------------------------------------------|--------------------------------------------------------------|---|
| Gerar orçamento com peças e serviços                | Verifica criação e mudança da OS para `AWAITING_APPROVAL`    | — |
| Gerar orçamento com peças e serviços                | Verifica total calculado corretamente entre peças e serviços | — |
| Notificação ao gerar orçamento                      | Verifica criação de notificação `QUOTE_GENERATED`            | — |
| Gerar orçamento sem peças ou serviços               | —                                                            | `InvalidQuoteDataException` |
| Orçamento duplicado para a OS                       | —                                                            | `QuoteAlreadyExistsException` |
| Peça duplicada ao gerar orçamento                   | —                                                            | `QuoteItemAlreadyExistsException` |
| Consumo de estoque da peça do orçamento             | Verifica redução da quantidade no banco                      | — |
| OS não encontrada ao gerar orçamento                | —                                                            | `ServiceOrderNotFoundException` |
| Aprovar orçamento pendente                          | Verifica mudança para `APPROVED`                             | — |
| Aprovar orçamento com status diferente de pendente  | —                                                            | `InvalidQuoteStatusException` |
| Reprovar orçamento adicionando motivo de recusa     | Verifica status e motivo de recusa                           | — |
| Reprovar orçamento com status diferente de pendente | —                                                            | `InvalidQuoteStatusException` |
| Buscar orçamento por ID                             | Verifica retorno do orçamento                                | — |
| Orçamento não encontrado por ID                     | —                                                            | `QuoteNotFoundException` |
| Buscar orçamentos por OS                            | Verifica listagem                                            | — |
| OS sem orçamentos                                   | Verifica lista vazia                                         | — |

### StockIntegrationTest

Cobre os fluxos do módulo de **Estoque** junto com o módulo de **Notificação**:

| Consumir quantidade e persistir | Verifica que a quantidade do item é reduzida no banco | — |
| Notificação de estoque baixo | Verifica criação de notificação `LOW_STOCK` no banco após consumo abaixo do mínimo permitido | — |
| Sem notificação acima do mínimo | Verifica que não cria notificação desnecessária quando estoque ainda está acima do mínimo permitido | — |

---
