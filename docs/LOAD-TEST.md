# Teste de Carga (k6)

Script de teste de carga em `k6/script.js`, usado para validar que a API se mantém estável sob volume crescente de requisições.

## Pré-requisitos

- [k6](https://k6.io/docs/get-started/installation/) instalado
- API rodando localmente (IntelliJ ou `docker compose up -d`)

## Executando

```bash
k6 run k6/script.js
```

## O que o script faz

- Sobe a carga em 5 estágios, de 20 até 150 VUs, ao longo de 2 minutos.
- Requisições `GET /api/v1/notifications` em loop, com 1s de intervalo, foi definido esse endpoint por não precisar de autenticação.
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