# 0003. Expor a aplicação por API REST

Data: 2026-09-06

## Status

Aceito

## Contexto

A aplicação precisa expor as operações da oficina para consumidores externos: o front-end
de atendimento, a documentação interativa usada na avaliação do Tech Challenge e, a partir
da Fase 3, o API Gateway que roteia as requisições de cliente para dentro do cluster. É
necessário escolher o estilo de comunicação dessa fronteira.

As opções avaliadas:

- **REST sobre HTTP/JSON.** Padrão dominante no ecossistema Spring, suportado nativamente
  por `spring-boot-starter-web` sem dependência extra. JSON é legível sem ferramenta
  especial, o que facilita depuração e avaliação. Documentação automática via OpenAPI e
  Swagger UI. O custo é a verbosidade do payload e a ausência de um contrato tipado
  verificado em tempo de compilação.
- **gRPC.** Contrato forte via Protobuf, payload binário compacto e melhor desempenho em
  comunicação entre serviços. Em compensação, exige geração de stubs, não é consumível
  direto do navegador sem uma camada de tradução e tornaria a avaliação por Swagger
  inviável. O ganho de desempenho não se justifica no volume deste sistema.
- **GraphQL.** Resolve over-fetching e dá flexibilidade de consulta ao cliente. O sistema
  tem um consumidor previsível e operações majoritariamente de escrita e transição de
  estado de ordem de serviço, não de composição de leituras. O schema e os resolvers
  seriam complexidade sem contrapartida.
- **Mensageria assíncrona.** Adequada para eventos e desacoplamento temporal, mas as
  operações aqui são síncronas por natureza: quem cadastra um cliente ou aprova um
  orçamento precisa da confirmação imediata. O módulo `notification` é interno e síncrono.

## Decisão

Vamos expor a aplicação por uma API REST sobre HTTP com payloads JSON, implementada com
`spring-boot-starter-web`.

As rotas são organizadas por recurso e versionadas no caminho, sob o prefixo `/api/v1`,
como em `/api/v1/customers`, `/api/v1/service-orders` e `/api/v1/stock-movements`. Os
verbos HTTP carregam a semântica da operação: `GET` para consulta, `POST` para criação,
`PUT` para substituição e `PATCH` para transições de estado, como a mudança de situação de
uma ordem de serviço.

O contrato de cada controller é declarado em uma interface `Api` separada, que concentra as
anotações OpenAPI e mantém a documentação fora da implementação. A documentação fica
disponível em `/swagger-ui.html`.

## Consequências

- (+) A API é consumível por qualquer cliente HTTP, sem stub gerado nem biblioteca
  específica, o que atende tanto o front-end quanto a avaliação manual do projeto.
- (+) Rotas identificadas por método e caminho permitem que o API Gateway proteja endpoints
  de cliente individualmente, sem conhecer o corpo da requisição, como descrito no
  [ADR-0006](0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md).
- (+) O Swagger UI dá uma superfície de teste pronta, usada na demonstração do projeto.
- (+) O versionamento no caminho permite publicar uma `/api/v2` no futuro sem quebrar os
  consumidores existentes.
- (+) A separação entre interface `Api` e controller mantém as anotações de documentação
  longe da lógica de orquestração.
- (-) Não há contrato verificado em tempo de compilação entre cliente e servidor: uma
  mudança de DTO só aparece como erro em execução.
- (-) O payload JSON é mais pesado que um formato binário, custo aceito porque o volume do
  sistema não o torna relevante.
- (-) A comunicação síncrona acopla o chamador à disponibilidade da API. Quando alguma
  operação exigir processamento assíncrono, será necessária uma nova decisão sobre
  mensageria.