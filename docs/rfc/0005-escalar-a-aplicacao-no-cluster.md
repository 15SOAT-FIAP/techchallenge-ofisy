# RFC-0005. Escalar a aplicação no cluster

Data: 2026-09-05
Autor: @rogerbertan

## Status

Aceita

## Resumo

Definir o mecanismo de escalabilidade da aplicação no cluster Kubernetes. A proposta é
usar um HorizontalPodAutoscaler baseado exclusivamente em utilização de CPU, com piso de
duas réplicas e teto de dez.

## Motivação

A Fase 3 exige um cluster Kubernetes com escalabilidade, motivada pela expansão da oficina
para múltiplas unidades e pelo crescimento da base de clientes. A aplicação precisa
absorver variação de carga sem intervenção manual e sem ficar superdimensionada quando a
demanda cai.

Hoje o Deployment roda com número fixo de réplicas, o que obriga a escolher entre
desperdiçar recursos no vale ou degradar no pico. Como os créditos do AWS Academy Learner
Lab são limitados, desperdício no vale não é abstração: é crédito consumido sem
contrapartida, e o laboratório precisa durar até a entrega.

Duas decisões estão em aberto: qual mecanismo de escalabilidade usar, e sobre qual métrica
ele deve reagir.

## Proposta

Escalar a aplicação com um HorizontalPodAutoscaler baseado exclusivamente em utilização de
CPU, definido em `k8s/hpa.yml`.

A configuração usa `autoscaling/v2`, com `minReplicas: 2`, `maxReplicas: 10` e alvo de
`averageUtilization: 50` sobre CPU. O piso de duas réplicas garante disponibilidade
durante atualizações e na falha de um pod.

O alvo de 50% é calculado sobre o `requests.cpu` de `250m` declarado no `deployment.yml`,
que tem `limits.cpu` de `500m`. A folga entre request e limit dá margem para o pod
absorver um pico enquanto novos pods sobem.

O HPA depende do metrics-server, que não vem instalado no EKS e precisa ser aplicado no
cluster. Sem ele o autoscaler não enxerga métrica alguma e não escala.

## Desvantagens

- O HPA fica cego se o metrics-server não estiver instalado, e a falha é silenciosa: o
  autoscaler simplesmente não escala, sem erro evidente. Isso pode passar despercebido até
  o momento em que a escalabilidade seria demonstrada.
- CPU é uma aproximação da carga real. Uma degradação causada por espera de I/O no banco
  não aparece como uso de CPU e não dispara escalonamento, que é justamente o cenário mais
  provável nesta aplicação.
- O tempo de inicialização da JVM, refletido no `initialDelaySeconds` de 60 segundos das
  probes, faz com que um pod novo demore a receber tráfego, o que limita a reação a picos
  bruscos.
- Escalar a aplicação não escala o RDS, que permanece em `db.t3.micro`. Sob carga alta, o
  banco tende a se tornar o gargalo e o número de conexões abertas cresce junto com o de
  pods, o que pode transformar o escalonamento em piora.
- Os valores de piso, teto e alvo são estimativas sem carga real medida. Podem estar
  errados, e só um teste de carga dirá.

## Alternativas consideradas

- **Número fixo de réplicas.** Simples, previsível e sem dependência do metrics-server,
  mas obriga a escolher entre desperdiçar recursos no vale ou degradar no pico. Não atende
  o requisito de escalabilidade da fase.
- **VerticalPodAutoscaler (VPA).** Ajusta requests e limits de cada pod em vez da
  quantidade deles. Para aplicação stateless atrás de load balancer, escalar
  horizontalmente aproveita melhor os nodes e não exige reinício do pod para aplicar o
  novo tamanho.
- **HPA por memória.** É uma métrica natural em muitas aplicações, mas não funciona bem
  com JVM: o heap é reservado na inicialização e raramente devolvido ao sistema
  operacional, então o consumo permanece alto mesmo com a aplicação ociosa. O HPA leria
  isso como pressão constante e escalaria sem necessidade real, sem nunca reduzir depois.
- **HPA por métricas customizadas de aplicação**, como requisições por segundo. Seria mais
  fiel à carga real e capturaria a degradação por espera de I/O que a CPU não vê. Exige o
  adapter de métricas customizadas no cluster, complexidade que o volume atual não
  justifica.
- **Cluster Autoscaler junto ao HPA**, para escalar também os nodes. Necessário se o teto
  de dez réplicas não couber nos nodes existentes. Descartado por ora: o node group tem
  margem para o teto proposto, e acrescentar mais um componente aumenta o consumo de
  créditos.

## Questões em aberto

- Os valores de `minReplicas: 2`, `maxReplicas: 10` e alvo de 50% são estimativas. Vale
  rodar um teste de carga com k6 antes de fixá-los, ou ajustar depois com base no
  comportamento observado?
- Como garantir que o metrics-server esteja sempre instalado, dado que o laboratório é
  reiniciado com frequência? Ele entra no roteiro de deploy ou fica como passo manual?
- O pool de conexões da aplicação precisa ser limitado por pod para que dez réplicas não
  esgotem as conexões do `db.t3.micro`?
- Vale configurar `behavior` no HPA para suavizar a redução de réplicas, evitando
  oscilação?

## Possibilidades futuras

- Migrar para métricas customizadas, como requisições por segundo ou latência, se a CPU se
  mostrar uma aproximação ruim da carga real.
- Acrescentar Cluster Autoscaler, se o teto de réplicas passar a não caber nos nodes.
- Revisar o dimensionamento do RDS, uma vez que o banco se confirme como gargalo sob
  carga.
- Usar os resultados do k6 para calibrar os limiares com base em medição, não em
  estimativa.

## Decisão registrada

- **Resultado**: Aceita
- **Data**: 2026-09-06
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**: [ADR-0005](../adr/0005-escalar-a-aplicacao-com-hpa-por-cpu.md)

O HPA por CPU foi aceito como o mecanismo que atende o requisito da fase com o menor
número de componentes novos no cluster, e o teto de dez réplicas limita o consumo de
créditos do laboratório.

As questões em aberto foram resolvidas assim: os valores iniciais foram mantidos como
estimativa, a serem calibrados com os testes de carga do k6; o metrics-server entra no
roteiro de deploy do cluster; o limite de pool por pod fica para uma decisão posterior,
junto com a revisão do dimensionamento do RDS; e o `behavior` não foi configurado, ficando
com o comportamento padrão do HPA.
