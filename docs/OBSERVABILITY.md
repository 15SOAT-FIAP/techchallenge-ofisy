# Observabilidade com Datadog

A aplicação envia métricas, logs e traces para o Datadog. Toda a cobertura vem de auto-instrumentação (Datadog Agent + `dd-java-agent`) — não há métricas de negócio escritas explicitamente no código; volume de ordens de serviço e tempo por status já são atendidos por rotas da própria API.

---

## O que é coletado

| Requisito                                   | Como é coletado                                                                                                                                          |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Latência das APIs                             | `dd-java-agent` (APM) instrumenta Spring MVC/Tomcat automaticamente — traces de cada requisição, sem código adicional.                                    |
| CPU/memória do Kubernetes                     | Datadog Agent (Process Agent + Cluster Agent), instalado via Helm no cluster EKS pelo repo de infraestrutura (Terraform).                                  |
| Healthcheck / uptime                          | Autodiscovery do Agent faz um `http_check` em `/actuator/health` (annotation `ad.datadoghq.com/ofisy-app.checks` em `k8s/deployment.yml`), além das probes nativas do K8s. |
| Logs estruturados (JSON) com correlação       | `logback-spring.xml` gera JSON via `logstash-logback-encoder`; `DD_LOGS_INJECTION=true` faz o `dd-java-agent` injetar `dd.trace_id`/`dd.span_id` no MDC, correlacionando log ↔ trace. |
| Falhas no processamento de ordem de serviço   | `GlobalExceptionHandler` loga `ERROR` com o campo estruturado `event: order_processing_failed` para `InvalidServiceOrderTransitionException` e `ServiceOrderNotFoundException` — base para um Log-Based Metric/Monitor no Datadog. |
| Erros e falhas (geral)                        | Métrica automática `http.server.requests` do Micrometer (tags `status`, `exception`, `outcome`).                                                          |

---

## Componentes

- **Datadog Agent** — instalado via Helm no namespace `datadog`, mas **não** por este repositório: é provisionado pelo repo de infraestrutura (`techchallenge-ofisy-eks-infra`, Terraform, `infra/datadog.tf`) como parte do próprio cluster EKS. Coleta logs de todos os pods, métricas de infraestrutura e recebe traces de APM na porta 8126.
- **`dd-java-agent.jar`** — baixado no build da imagem (`Dockerfile`) e anexado via `-javaagent` no `ENTRYPOINT`. Responsável pelo APM e pela injeção de contexto de trace nos logs.

---

## Configuração necessária

1. A conta Datadog deste projeto está no site **`us5.datadoghq.com`** (não o padrão `datadoghq.com`/US1) — já refletido no default de `uri` em `application.yml` (`DD_METRICS_URI`). A instalação do Agent (site do Helm chart) é responsabilidade do repo de infraestrutura.
2. `DD_API_KEY` é um **Organization Secret** do GitHub (org `15SOAT-FIAP`), usado pelo `ofisy-secret` do Kubernetes (injetado via `cd.yml`).

---

## Dashboards recomendados

- **Erros e falhas**: `sum:http.server.requests{service:ofisy,status:5xx}.as_count()` e `sum:trace.servlet.request.errors{service:ofisy}.as_count()`.

## Monitors recomendados

- Latência p95 de `http.server.requests` acima de um limiar (ex.: 1s).
- Taxa de erro 5xx acima de um limiar.
- Falha no processamento de ordem de serviço: criar um Log-Based Metric no Datadog filtrando `@event:order_processing_failed` e alertar em cima dele.
- Healthcheck falhando (monitor de Autodiscovery HTTP check).
- CPU/memória do pod perto do `limit` (`k8s/deployment.yml`: 500m CPU / 1Gi memória) ou HPA no teto (`k8s/hpa.yml`: `maxReplicas: 10`).
