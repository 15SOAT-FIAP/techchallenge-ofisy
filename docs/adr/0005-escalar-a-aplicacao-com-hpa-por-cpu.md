# 0005. Escalar a aplicação com HPA por CPU

Data: 2026-09-06

## Status

Aceito

## Contexto

A Fase 3 exige um cluster Kubernetes com escalabilidade, motivada pela expansão da oficina
para múltiplas unidades e pelo crescimento da base de clientes. A aplicação precisa absorver
variação de carga sem intervenção manual e sem ficar superdimensionada quando a demanda cai.

As opções avaliadas:

- **Número fixo de réplicas.** Simples, mas obriga a escolher entre desperdiçar recursos no
  vale ou degradar no pico. Não atende o requisito de escalabilidade.
- **HorizontalPodAutoscaler (HPA).** Ajusta a quantidade de pods conforme uma métrica
  observada. É o mecanismo nativo do Kubernetes para isso e opera sobre o Deployment já
  existente.
- **VerticalPodAutoscaler (VPA).** Ajusta requests e limits de cada pod em vez da quantidade
  deles. Para aplicação stateless atrás de load balancer, escalar horizontalmente aproveita
  melhor os nodes e não exige reinício do pod para aplicar o novo tamanho.

Definido o HPA, resta escolher a métrica. Memória é uma opção natural, mas não funciona bem
com JVM: o heap é reservado na inicialização e raramente devolvido ao sistema operacional,
então o consumo permanece alto mesmo com a aplicação ociosa. O HPA leria isso como pressão
constante e escalaria sem necessidade real, sem nunca reduzir depois.

Métricas customizadas de aplicação, como requisições por segundo, seriam mais fiéis à carga,
mas exigiriam o adapter de métricas customizadas no cluster, complexidade que o volume atual
não justifica.

## Decisão

Vamos escalar a aplicação com um HorizontalPodAutoscaler baseado exclusivamente em
utilização de CPU, definido em `k8s/hpa.yml`.

A configuração usa `autoscaling/v2`, com `minReplicas: 2`, `maxReplicas: 10` e alvo de
`averageUtilization: 50` sobre CPU. O piso de duas réplicas garante disponibilidade durante
atualizações e na falha de um pod.

O alvo de 50% é calculado sobre o `requests.cpu` de `250m` declarado no `deployment.yml`, que
tem `limits.cpu` de `500m`. A folga entre request e limit dá margem para o pod absorver um
pico enquanto novos pods sobem.

O HPA depende do metrics-server, que não vem instalado no EKS e precisa ser aplicado no
cluster. Sem ele o autoscaler não enxerga métrica alguma e não escala.

## Consequências

- (+) A aplicação acompanha a variação de carga sem intervenção manual, atendendo o requisito
  de escalabilidade da Fase 3.
- (+) O piso de duas réplicas elimina o ponto único de falha e permite atualização sem
  indisponibilidade.
- (+) Escalar apenas por CPU evita o crescimento descontrolado que a métrica de memória
  provocaria com a JVM.
- (+) O teto de dez réplicas limita o consumo de créditos do laboratório.
- (-) O HPA fica cego se o metrics-server não estiver instalado, e a falha é silenciosa: o
  autoscaler simplesmente não escala, sem erro evidente.
- (-) CPU é uma aproximação da carga real. Uma degradação causada por espera de I/O no banco
  não aparece como uso de CPU e não dispara escalonamento.
- (-) O tempo de inicialização da JVM, refletido no `initialDelaySeconds` de 60 segundos das
  probes, faz com que um pod novo demore a receber tráfego, o que limita a reação a picos
  bruscos.
- (-) Escalar a aplicação não escala o RDS, que permanece em `db.t3.micro`. Sob carga alta, o
  banco tende a se tornar o gargalo e o número de conexões abertas cresce junto com o de pods.