# 0002. Adotar Clean Architecture com DDD

Data: 2026-09-06

## Status

Aceito

## Contexto

O Ofisy nasceu como monolito por exigência da primeira fase do Tech Challenge, mas as
fases seguintes pedem a evolução para microsserviços. A organização interna do código
precisa comportar essa transição sem uma reescrita, e precisa acomodar dez agregados de
negócio (customer, vehicle, stock, stockmovement, servicecatalog, serviceorder,
serviceorderexecution, quote, user e notification) mantendo as regras de cada um
compreensíveis de forma isolada.

A abordagem convencional em Spring Boot é organizar o código em `controller`, `service` e
`repository`, com as entidades JPA servindo ao mesmo tempo como modelo de domínio e como
mapeamento de tabela. É rápido de escrever e familiar para qualquer pessoa do time, mas
amarra as regras de negócio ao framework e ao esquema do banco: a entidade passa a existir
em função das anotações de persistência, e testar uma regra exige subir contexto Spring ou
banco. Extrair um módulo depois vira um trabalho de desemaranhamento.

A alternativa avaliada foi a Clean Architecture, com a regra de dependência apontando
sempre para dentro, combinada com DDD para modelar cada agregado. O custo é maior
cerimônia: mais arquivos por caso de uso, mappers explícitos entre camadas e a disciplina
constante de não deixar o framework vazar para o centro.

## Decisão

Vamos organizar o código segundo a Clean Architecture, com os agregados modelados por DDD,
nas camadas `domain`, `application`, `adapters` e `config`.

O `domain` guarda entidades ricas, value objects como `CpfCnpj`, exceções de invariante e
as interfaces de repositório, que são as portas. Não conhece persistência: `Customer` não
tem anotação JPA.

O `application` guarda um caso de uso por operação, cada um como par de interface `UseCase`
e implementação `Service`, no seu próprio pacote.

Os `adapters` implementam as portas e traduzem para o mundo externo, divididos em
`controllers` (entrada REST e DTOs), `gateways` (entidade JPA, mapper e a implementação da
porta do domínio) e `presenters` (conversão do domínio para DTO de resposta). A inversão de
dependência fica concreta em `CustomerRepositoryImpl`, que vive no gateway, implementa
`CustomerRepository` do domínio e faz o mapeamento entre `CustomerEntity` e `Customer`.

## Consequências

- (+) As regras de negócio ficam testáveis sem Spring e sem banco, porque o domínio não
  depende de nenhum dos dois.
- (+) Cada agregado tem uma fronteira explícita, o que torna a extração para microsserviço
  nas próximas fases um recorte por pacote em vez de uma reescrita.
- (+) Trocar a tecnologia de persistência afeta apenas o gateway, já que o domínio conhece
  somente a interface do repositório.
- (+) A separação em um caso de uso por operação deixa claro no nome do pacote o que a
  aplicação faz.
- (-) O volume de código cresce: cada operação exige interface, service, DTOs, mapper e
  presenter, mesmo quando é um simples cadastro.
- (-) A disciplina depende de revisão humana, porque nada na build impede que uma anotação
  de framework seja acrescentada a uma classe de domínio.
- (-) As portas de repositório do domínio usam `Page` e `Pageable` do Spring Data, o que é
  um vazamento consciente de framework no centro, aceito para não reimplementar um contrato
  de paginação próprio.