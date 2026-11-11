# Teste de Carga (k6)

Script de teste de carga em `k6/script.js`, usado para validar que a API se mantém estável sob volume crescente de requisições.

## Pré-requisitos

- [k6](https://k6.io/docs/get-started/installation/) instalado
- API rodando localmente (IntelliJ ou `docker compose up -d`)
- ID de uma ordem de serviço existente

## Executando

```bash
k6 run -e SERVICE_ORDER_ID="ID_DA_ORDEM" k6/script.js
```

## O que o script faz

- Sobe a carga em quatro estágios, com pico de 200 VUs.
- Consulta a rota pública `GET /api/v1/service-orders/{id}/status` em loop, com intervalo de 1 segundo. O endereço padrão é `http://localhost:8080`. Use `-e CORE_BASE_URL=<url>` para alterar o endereço.
- Valida status 200 em cada resposta.
- Threshold: taxa de falha (`http_req_failed`) deve ficar abaixo de 5%.

## Interpretando o resultado

- `http_req_duration` (p95): latência do percentil 95, quanto menor melhor.
- `http_req_failed`: taxa de requisições com erro.
- Bloco `THRESHOLDS`: mostra se passou (✓) ou falhou (✗).

## Acompanhando recursos durante o teste

```bash
docker stats                     # se a API estiver em container

ps aux | grep OfisyApplication   # PID do processo local (IntelliJ)
top -pid <PID>                   # CPU/memória do processo
```

## Ajustando a carga

Altere os `stages` em `script.js` para aumentar/diminuir o pico de VUs. Como k6 e API rodam na mesma máquina em teste local, valores muito altos podem medir a contenção de recursos do computador em vez da capacidade real da API.

## Próximos passos (Kubernetes)

Para testar o autoscaling real (HPA), reaproveitar o mesmo script apontando para o endpoint exposto pelo cluster e acompanhar `kubectl get hpa -w` durante a execução.
