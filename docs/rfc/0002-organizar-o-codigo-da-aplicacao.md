# RFC-0002. Organizar o código da aplicação

Data: 03/09/2026
Autor: @rogerbertan

## Status

Encerrada - Aprovada

## Resumo

Organizar o código pela Clean Architecture, com a regra de dependência apontando sempre
para dentro e DDD para modelar os agregados, em vez da divisão convencional em
`controller`, `service` e `repository`.

## Problema

O Ofisy nasceu como monolito por exigência da primeira fase do Tech Challenge, mas as
fases seguintes pedem a evolução para microsserviços. A organização interna do código
precisa comportar essa transição sem uma reescrita.

São dez agregados de negócio (customer, vehicle, stock, stockmovement, servicecatalog,
serviceorder, serviceorderexecution, quote, user e notification), e as regras de cada um
precisam ser compreensíveis de forma isolada. Uma ordem de serviço tem transições de
estado próprias, um orçamento tem regras de aprovação, o estoque tem invariantes de
quantidade mínima. Se essas regras ficarem espalhadas entre controllers e entidades JPA,
nenhuma delas fica evidente.

Há também uma questão de teste. Para exercitar uma regra de negócio, é preciso decidir se
ela vive em uma classe que depende de Spring e de banco. Essa decisão, tomada uma vez no
começo, define o custo de todos os testes que virão.

A escolha precisa ser feita agora, no início, porque reorganizar as camadas depois de dez
agregados implementados é uma reescrita.

## Proposta Técnica

Organizar o código segundo a Clean Architecture, com os agregados modelados por DDD, em
quatro camadas: `domain`, `application`, `adapters` e `config`.

**`domain`** guarda entidades ricas, value objects como `CpfCnpj`, exceções de invariante
e as interfaces de repositório, que são as portas. Não conhece persistência: `Customer`
não tem anotação JPA.

**`application`** guarda um caso de uso por operação, cada um como par de interface
`UseCase` e implementação `Service`, no seu próprio pacote.

**`adapters`** implementam as portas e traduzem para o mundo externo, divididos em
`controllers` (entrada REST e DTOs), `gateways` (entidade JPA, mapper e a implementação da
porta do domínio) e `presenters` (conversão do domínio para DTO de resposta).

A inversão de dependência fica concreta em `CustomerRepositoryImpl`, que vive no gateway,
implementa `CustomerRepository` do domínio e faz o mapeamento entre `CustomerEntity` e
`Customer`.

## Impacto esperado

**Benefícios.**

- As regras de cada um dos dez agregados ficam compreensíveis de forma isolada, em vez de
  espalhadas entre controllers e entidades JPA.
- O domínio não depende de Spring nem de banco, de modo que exercitar uma regra de negócio
  não exige subir contexto nem infraestrutura. Isso define o custo de todos os testes que
  virão.
- A fronteira entre agregados fica explícita, o que torna a extração para microsserviços na
  Fase 4 um recorte por pacote em vez de uma reescrita.
- A tecnologia de persistência pode ser trocada sem tocar no domínio, que conhece apenas a
  interface do repositório.

**Riscos e custos.**

- O volume de código cresce de forma significativa: cada operação exige interface,
  service, DTOs, mapper e presenter, mesmo quando é um simples cadastro. Para um CRUD, a
  cerimônia é desproporcional ao que ele faz.
- A disciplina depende de revisão humana. Nada na build impede que uma anotação de
  framework seja acrescentada a uma classe de domínio, e basta um descuido para o centro
  começar a vazar.
- Mappers explícitos entre camadas são código repetitivo, sem regra de negócio, que
  precisa ser mantido em sincronia com as duas pontas.
- A curva de aprendizado é real para quem vem do padrão convencional do Spring. O time
  gasta tempo discutindo onde cada coisa vai antes de ganhar velocidade.
- As portas de repositório do domínio usarão `Page` e `Pageable` do Spring Data, um
  vazamento consciente de framework no centro, aceito para não reimplementar um contrato
  de paginação próprio.

## Alternativas consideradas

- **Divisão convencional em `controller`, `service` e `repository`**, com as entidades JPA
  servindo ao mesmo tempo como modelo de domínio e mapeamento de tabela. É rápido de
  escrever e familiar para qualquer pessoa do time. Amarra as regras de negócio ao
  framework e ao esquema do banco: a entidade passa a existir em função das anotações de
  persistência, e testar uma regra exige subir contexto Spring ou banco. Extrair um módulo
  depois vira um trabalho de desemaranhamento.
- **Arquitetura hexagonal pura**, sem a separação em casos de uso individuais. Resolve o
  mesmo problema de inversão de dependência com menos arquivos, mas concentra várias
  operações por serviço, o que torna menos evidente o que a aplicação faz e dificulta o
  recorte por funcionalidade na extração futura.
- **Organização por funcionalidade (package by feature)**, agrupando tudo de um agregado
  em um pacote sem separar camadas. Facilita muito a extração para microsserviço, que vira
  um recorte de diretório, mas não impede o acoplamento ao framework dentro de cada
  pacote. O problema principal continua.
- **Monolito modular com módulos Maven separados**, com a fronteira garantida pela build.
  É a opção que mais protege contra vazamento, porque a dependência indevida não compila.
  Descartada pelo custo de configuração e porque fragmenta o projeto cedo demais, quando o
  recorte entre módulos ainda não está claro.

## Pontos em aberto

- Vale acrescentar uma verificação automatizada de dependências entre camadas, algo como
  ArchUnit, já nesta fase? Ou a revisão de Pull Request é suficiente enquanto o time é
  pequeno?
- `Page` e `Pageable` do Spring Data no domínio: aceitar o vazamento ou definir um
  contrato de paginação próprio? O contrato próprio é mais correto e é mais código sem
  regra de negócio.
- Um caso de uso por operação gera muitos pacotes. Vale agrupar as operações de leitura de
  um mesmo agregado, ou a granularidade fina compensa pela clareza?

## Decisão registrada

- **Resultado**: Encerrada - Aprovada
- **Data**: 06/09/2026
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**: [ADR-0002](../adr/0002-adotar-clean-architecture-com-ddd.md)

O custo em cerimônia foi aceito em troca da testabilidade do domínio e da fronteira
explícita entre agregados, que é o que torna a extração das próximas fases um recorte por
pacote em vez de uma reescrita.

As questões em aberto foram resolvidas assim: a verificação automatizada de dependências
fica para depois, com a disciplina mantida por revisão; `Page` e `Pageable` permanecem no
domínio como vazamento consciente, registrado no ADR; e a granularidade de um caso de uso
por operação foi mantida, inclusive para as leituras.
