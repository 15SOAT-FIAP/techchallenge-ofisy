# RFC-0003. Definir o estilo de comunicação da API

Data: 2026-09-03
Autor: @rogerbertan

## Status

Aceita

## Resumo

Escolher o estilo de comunicação da fronteira externa da aplicação. A proposta é expor as
operações da oficina por uma API REST sobre HTTP com payloads JSON, com rotas versionadas
no caminho e documentação automática via OpenAPI.

## Motivação

O projeto entrega apenas o backend do sistema, sem front-end. A fronteira externa da
aplicação é, portanto, a própria API, e ela tem dois consumidores reais.

O primeiro é a **avaliação do Tech Challenge**, feita por quem está fora do time. Todas as
operações da oficina precisam ser exercitáveis sem que o avaliador instale ferramenta,
gere stub ou escreva código de cliente. Como não há interface gráfica no projeto, a
documentação interativa é a única superfície pela qual o sistema pode ser demonstrado, o
que a torna requisito e não conveniência.

O segundo é o **API Gateway**, que a partir da Fase 3 roteia as requisições de cliente
para dentro do cluster. Ele precisa conseguir identificar e proteger endpoints
individualmente, sem inspecionar o corpo da requisição.

O estilo escolhido determina o contrato com ambos e é caro de trocar depois, então a
decisão precisa ser tomada antes de a primeira rota ser escrita.

## Proposta

Expor a aplicação por uma API REST sobre HTTP com payloads JSON, implementada com
`spring-boot-starter-web`.

**Rotas.** Organizadas por recurso e versionadas no caminho, sob o prefixo `/api/v1`, como
em `/api/v1/customers`, `/api/v1/service-orders` e `/api/v1/stock-movements`.

**Verbos.** Carregam a semântica da operação: `GET` para consulta, `POST` para criação,
`PUT` para substituição e `PATCH` para transições de estado, como a mudança de situação de
uma ordem de serviço.

**Documentação.** O contrato de cada controller é declarado em uma interface `Api`
separada, que concentra as anotações OpenAPI e mantém a documentação fora da
implementação. A documentação fica disponível em `/swagger-ui.html`.

## Desvantagens

- Não há contrato verificado em tempo de compilação entre cliente e servidor. Uma mudança
  de DTO só aparece como erro em execução.
- O payload JSON é mais pesado que um formato binário. É custo aceito porque o volume do
  sistema não o torna relevante, mas ele existe.
- A comunicação síncrona acopla o chamador à disponibilidade da API. Quando alguma
  operação exigir processamento assíncrono, será necessária uma decisão nova sobre
  mensageria.
- Manter a interface `Api` separada do controller significa duas assinaturas de método
  para cada rota, que precisam ficar em sincronia.
- REST não tem resposta única para operações que não são CRUD. Aprovar um orçamento ou
  mudar o estado de uma ordem de serviço exige escolher entre `PATCH` no recurso ou um
  sub-recurso de ação, e essa escolha se repete a cada operação nova.

## Alternativas consideradas

- **gRPC.** Contrato forte via Protobuf, payload binário compacto e melhor desempenho em
  comunicação entre serviços. Em compensação, exige geração de stubs e não é consumível
  direto do navegador sem uma camada de tradução, o que tornaria a avaliação por Swagger
  inviável. Sem front-end no projeto, perder a superfície de demonstração é um custo alto,
  e o ganho de desempenho não se justifica no volume deste sistema.
- **GraphQL.** Resolve over-fetching e dá flexibilidade de consulta ao cliente. O sistema
  tem consumidores previsíveis e operações majoritariamente de escrita e transição de
  estado de ordem de serviço, não de composição de leituras. O schema e os resolvers
  seriam complexidade sem contrapartida.
- **Mensageria assíncrona.** Adequada para eventos e desacoplamento temporal, mas as
  operações aqui são síncronas por natureza: quem cadastra um cliente ou aprova um
  orçamento precisa da confirmação imediata. O módulo `notification` é interno e síncrono.
- **REST com versionamento por cabeçalho** em vez de caminho. É mais elegante em teoria e
  mantém a URL estável entre versões, mas dificulta o roteamento no API Gateway, que
  precisaria inspecionar cabeçalho para decidir, e torna o teste manual menos direto.

## Questões em aberto

- Transições de estado devem ser `PATCH` no próprio recurso ou sub-recursos de ação, como
  `POST /service-orders/{id}/approve`? A primeira é mais REST, a segunda é mais explícita
  sobre a operação de negócio.
- Vale adotar um formato padronizado de erro, como RFC 7807 (Problem Details), desde já?
- O versionamento em `/api/v1` deve valer também para as rotas de cliente que passarão
  pelo API Gateway, ou elas terão um prefixo próprio?

## Possibilidades futuras

- Publicar uma `/api/v2` sem quebrar os consumidores existentes, se algum contrato
  precisar mudar de forma incompatível.
- Gerar clientes a partir do documento OpenAPI, caso venha a existir um front-end em fase
  posterior.
- Adotar gRPC apenas na comunicação entre serviços na Fase 4, mantendo REST na fronteira
  externa, caso o desempenho passe a importar.

## Decisão registrada

- **Resultado**: Aceita
- **Data**: 2026-09-06
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**: [ADR-0003](../adr/0003-expor-a-aplicacao-por-api-rest.md)

REST atende os dois consumidores sem exigir ferramenta adicional de nenhum deles, e o
Swagger UI dá a superfície de demonstração que o projeto não tem por outro meio. A
identificação de rotas por método e caminho é o que permite ao API Gateway proteger
endpoints de cliente individualmente, conforme a separação de autenticação decidida na
[RFC-0004](0004-autenticar-clientes-sem-cadastro-de-senha.md).

As questões em aberto foram resolvidas assim: as transições de estado usam `PATCH` no
próprio recurso; o formato padronizado de erro fica para uma decisão posterior; e as rotas
de cliente mantêm o prefixo `/api/v1`, sendo declaradas individualmente no API Gateway.
