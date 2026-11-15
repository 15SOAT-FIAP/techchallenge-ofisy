# Observabilidade com Datadog

A observabilidade do Ofisy tem duas partes:

- **Infraestrutura**: o Datadog Agent roda no cluster EKS e recebe o que as aplicações enviam. Ele é provisionado pelo repositório de infraestrutura, não por este ([Componentes](#componentes)).
- **Instrumentação da aplicação**: é o que gera os traces, os logs correlacionados e as métricas. Ela fica neste repositório e está descrita em [Instrumentação da aplicação](#instrumentação-da-aplicação).

A cobertura vem de auto-instrumentação do `dd-java-agent`, com dois complementos em código: o log estruturado das falhas de OS e a propagação do trace pelo outbox e pela fila. Não há métricas de negócio escritas no código; o volume de ordens de serviço e o tempo por status são atendidos por rotas da própria API.

---

## O que é coletado

| Requisito                                   | Como é coletado                                                                                                                                          |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Latência das APIs                             | `dd-java-agent` (APM) instrumenta Spring MVC/Tomcat automaticamente — traces de cada requisição, sem código adicional.                                    |
| CPU/memória do Kubernetes                     | Datadog Agent (Process Agent + Cluster Agent), instalado via Helm no cluster EKS pelo repo de infraestrutura (Terraform).                                  |
| Healthcheck / uptime                          | Autodiscovery do Agent faz um `http_check` em `/actuator/health` (annotation `ad.datadoghq.com/ofisy-app.checks` em `k8s/deployment.yml`), além das probes nativas do K8s. |
| Logs estruturados (JSON) com correlação       | `logback-spring.xml` gera JSON via `logstash-logback-encoder`; `DD_LOGS_INJECTION=true` faz o `dd-java-agent` injetar `dd.trace_id`/`dd.span_id` no MDC, correlacionando log ↔ trace. |
| Falhas no processamento de ordem de serviço   | `GlobalExceptionHandler` loga `ERROR` com o campo estruturado `event: order_processing_failed` para `InvalidServiceOrderTransitionException` e `ServiceOrderNotFoundException` — base para o monitor de falhas ([Monitors configurados](#monitors-configurados)). |
| Erros e falhas (geral)                        | Métricas de APM (`trace.servlet.request.*`) geradas pelo `dd-java-agent` e métricas do Micrometer (`http.server.requests.*`, tags `status`, `exception`, `outcome`, `uri`). |
| Rastreamento dos fluxos distribuídos          | O contexto do trace é salvo com cada evento do outbox e restaurado na publicação, e o listener continua o trace recebido na mensagem, então o fluxo da OS segue no mesmo trace entre o core e os outros microsserviços ([Rastreamento distribuído](#rastreamento-distribuído)). |

---

## Instrumentação da aplicação

Cada peça abaixo está versionada neste repositório. Juntas, elas fazem a aplicação gerar traces, logs correlacionados e métricas no Datadog.

| Peça | Arquivo | O que faz |
|---|---|---|
| `dd-java-agent` na imagem | `Dockerfile` | Baixa o tracer Java do Datadog (`https://dtdg.co/latest-java-tracer`, sempre a versão mais recente) no build da imagem |
| Ativação do agent | `Dockerfile` (`ENTRYPOINT`) | Sobe a aplicação com `-javaagent:/app/dd-java-agent.jar`, que instrumenta Tomcat, Spring MVC, JDBC/PostgreSQL, Spring Data, `@Scheduled` e o SDK da AWS (SNS/SQS) sem código adicional |
| Identidade do serviço (unified service tagging) | `k8s/configmap.yml` e `k8s/deployment.yml` | `DD_SERVICE=ofisy-api`, `DD_ENV=production` e `DD_VERSION`, que o `cd.yml` preenche com a tag da imagem (hash curto do commit). A label `tags.datadoghq.com/service: ofisy-api` no pod liga as métricas do Kubernetes ao mesmo serviço do APM |
| Envio para o Agent do nó | `k8s/deployment.yml` e `k8s/configmap.yml` | `DD_AGENT_HOST` recebe o IP do nó (`status.hostIP`) e `DD_TRACE_AGENT_PORT=8126` é a porta onde o Agent recebe os traces |
| Correlação log ↔ trace | `k8s/configmap.yml` (`DD_LOGS_INJECTION=true`) e `src/main/resources/logback-spring.xml` | O agent coloca `dd.trace_id`, `dd.span_id`, `dd.service`, `dd.env` e `dd.version` no MDC, e o `LogstashEncoder` escreve esses campos em cada linha de log JSON. O Agent coleta o stdout do pod, e o Datadog liga cada log ao seu trace |
| Métricas da aplicação | `src/main/resources/application.yml` (`management.datadog.metrics.export`) | O Micrometer envia métricas direto para a API do Datadog (`api.us5.datadoghq.com`) a cada 30s, quando `DD_METRICS_ENABLED=true` (só no ConfigMap do k8s) |
| Healthcheck | `k8s/deployment.yml` (annotation `ad.datadoghq.com/ofisy-app.checks`) | O Autodiscovery do Agent configura um `http_check` em `/actuator/health` |
| Falhas de OS | `config/GlobalExceptionHandler` | Loga `ERROR` com `event: order_processing_failed` (via `StructuredArguments.kv`) nas falhas de transição e de OS não encontrada |
| Rastreamento distribuído | `adapters/tracing/TraceContextPropagator`, `adapters/tracing/MessageTraceContextReader` e `DD_TRACE_OTEL_ENABLED=true` | Mantém o fluxo da OS no mesmo trace entre o outbox, o SNS/SQS e os outros microsserviços ([Rastreamento distribuído](#rastreamento-distribuído)) |

### O que aparece no Datadog

- **APM**: o serviço `ofisy-api`, com os spans das requisições HTTP (`servlet.request`, `spring.handler`), das consultas ao banco (`postgresql.query`, `repository.operation`), do job do outbox (`scheduled.call`) e os spans `outbox.publish` e `event.consume` da mensageria.
- **Logs**: as linhas JSON do pod, com os campos `dd.trace_id` e `dd.span_id`. Na tela de um trace, a aba **Logs** mostra os logs daquela requisição. Os logs chegam com `service:ofisy`, que vem de `spring.application.name` pelo `customFields` do `logback-spring.xml`; a ligação com o trace é feita pelo `dd.trace_id`, então não depende desse nome.
- **Métricas**: as de APM (`trace.servlet.request.hits`, `trace.servlet.request.errors`, latência em `trace.servlet.request`), com a tag `service:ofisy-api`, e as do Micrometer (`http.server.requests.count`, `.avg`, `.max`), que chegam sem a tag `service`, porque o core não define `management.metrics.tags`.

---

## Rastreamento distribuído

O core publica eventos por um *transactional outbox*: o evento é gravado na requisição e publicado depois por um job agendado, que roda em um trace próprio. Do lado do consumo, o `dd-java-agent` não continua no `@SqsListener` o trace que chega na mensagem. Sem tratamento, o fluxo de uma OS apareceria quebrado no Datadog em cada uma dessas passagens.

Para manter o fluxo em um único trace:

1. Ao gravar o evento no outbox, o contexto do trace ativo é guardado na coluna `trace_context`.
2. O `OutboxEventPublisher` restaura esse contexto e publica no SNS dentro do span `outbox.publish`. O agent injeta o trace no atributo `_datadog` da mensagem.
3. O `ServiceOrderEventsListener` lê esse atributo e processa o evento dentro do span `event.consume`, filho do trace de quem publicou.

A captura e a restauração usam a API do OpenTelemetry, que o `dd-java-agent` conecta aos spans do Datadog quando `DD_TRACE_OTEL_ENABLED=true`. O passo a passo, os atributos dos spans e o comportamento sem o agent estão em [MESSAGING.md](MESSAGING.md#rastreamento-distribuído-pelo-outbox).

### Microsserviços

Os microsserviços seguem a mesma instrumentação: `dd-java-agent` na imagem, ativado pelo `JAVA_TOOL_OPTIONS` do ConfigMap, `DD_LOGS_INJECTION`, logs JSON e o mesmo ajuste de outbox e listener. Cada um aparece no APM com o seu `DD_SERVICE`:

| Serviço | `DD_SERVICE` | Publica eventos (outbox) | Consome eventos (listener) |
|---|---|---|---|
| Core (este repositório) | `ofisy-api` | Sim | Sim |
| [ms-ofisy-billing](https://github.com/15SOAT-FIAP/ms-ofisy-billing) | `ms-ofisy-billing` | Sim | Sim |
| [ms-ofisy-execution](https://github.com/15SOAT-FIAP/ms-ofisy-execution) | `ms-ofisy-execution` | Sim | Sim |
| [ms-ofisy-stock](https://github.com/15SOAT-FIAP/ms-ofisy-stock) | `ms-ofisy-stock` | Sim | Sim |
| [ms-ofisy-notification](https://github.com/15SOAT-FIAP/ms-ofisy-notification) | `ms-ofisy-notification` | Não | Sim |

---

## Componentes

- **Datadog Agent** — instalado via Helm no namespace `datadog`, mas **não** por este repositório: é provisionado pelo repo de infraestrutura (`techchallenge-ofisy-eks-infra`, Terraform, `infra/datadog.tf`) como parte do próprio cluster EKS. Coleta logs de todos os pods, métricas de infraestrutura e recebe traces de APM na porta 8126.
- **`dd-java-agent.jar`** — baixado no build da imagem (`Dockerfile`) e anexado via `-javaagent` no `ENTRYPOINT`. Responsável pelo APM e pela injeção de contexto de trace nos logs.

---

## Configuração necessária

1. A conta Datadog deste projeto está no site **`us5.datadoghq.com`** (não o padrão `datadoghq.com`/US1) — já refletido no default de `uri` em `application.yml` (`DD_METRICS_URI`). A instalação do Agent (site do Helm chart) é responsabilidade do repo de infraestrutura.
2. `DD_API_KEY` é um **Organization Secret** do GitHub (org `15SOAT-FIAP`), usado pelo `ofisy-secret` do Kubernetes (injetado via `cd.yml`).
3. `DD_TRACE_OTEL_ENABLED=true` no `k8s/configmap.yml` conecta a API do OpenTelemetry ao `dd-java-agent`. É ela que o outbox e o listener usam para capturar e restaurar o contexto do trace.

---

## Dashboards recomendados

- **Volume de requisições**: `sum:trace.servlet.request.hits{service:ofisy-api}.as_count()`.
- **Erros e falhas**: `sum:trace.servlet.request.errors{service:ofisy-api}.as_count()` e, pelo Micrometer, `sum:http.server.requests.count{outcome:server_error}.as_count()`.
- **Latência**: `p95:trace.servlet.request{service:ofisy-api}`.

## Monitors configurados

- **Ocorreu uma falha no processamento da ordem de serviço** (log alert): `status:error *GlobalExceptionHandler.handleServiceOrderNotFound*`, com mais de 1 ocorrência em 5 minutos, notificando pelo webhook `datadog_alerts`. Hoje ele cobre só a OS não encontrada; para cobrir também as transições inválidas, a query pode filtrar pelo campo estruturado `@event:order_processing_failed`.
- **[Synthetics] ofisy - API Gateway Uptime**: teste de uptime do API Gateway.

## Monitors recomendados

- Latência p95 de `trace.servlet.request{service:ofisy-api}` acima de um limiar (ex.: 1s).
- Taxa de erro 5xx acima de um limiar.
- Healthcheck falhando (monitor de Autodiscovery HTTP check).
- CPU/memória do pod perto do `limit` (`k8s/deployment.yml`: 1 CPU / 1Gi de memória) ou HPA no teto (`k8s/hpa.yml`: `maxReplicas: 10`).
