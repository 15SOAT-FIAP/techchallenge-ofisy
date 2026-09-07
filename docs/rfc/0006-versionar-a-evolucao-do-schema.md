# RFC-0006. Versionar a evolução do schema

Data: 2026-09-04
Autor: @rogerbertan

## Status

Aceita

## Resumo

Definir como a evolução do schema do banco chega a cada ambiente. A proposta é versionar o
schema com Flyway, em scripts SQL numerados, e impedir que o Hibernate altere o banco em
qualquer ambiente.

## Motivação

O schema do banco evolui junto com a aplicação: novas tabelas por agregado, colunas
acrescentadas a entidades existentes e dados de carga inicial, como usuários e catálogo de
serviços.

Essa evolução acontece em paralelo por várias pessoas do grupo, e precisa chegar de forma
idêntica a quatro destinos diferentes: o ambiente local de cada um, o contêiner de
desenvolvimento, o banco dos testes de integração e o RDS de produção. São quatro bancos
que precisam convergir para o mesmo estado a partir do mesmo conjunto de mudanças.

Sem um mecanismo explícito, a convergência depende de cada pessoa lembrar do que rodou
onde, e a divergência só aparece quando uma consulta falha em um ambiente e funciona em
outro. Como o laboratório é reiniciado com frequência e o banco precisa ser recriado do
zero, o problema deixa de ser hipotético.

## Proposta

Versionar o schema com Flyway, em scripts SQL numerados sob
`src/main/resources/db/migration`, seguindo a convenção `V<n>__descricao.sql`.

O Hibernate não altera o schema em nenhum ambiente. Nos perfis `docker` e `k8s` a
configuração é `ddl-auto: validate`, o que faz a aplicação conferir na inicialização se o
mapeamento das entidades corresponde às tabelas existentes e falhar caso divirjam. No
perfil de desenvolvimento local a configuração é `ddl-auto: none`, deixando a validação
para os ambientes conteinerizados.

As migrations cobrem tanto estrutura quanto dados de carga inicial, incluindo os usuários
usados na avaliação do projeto. Elas também rodam nos testes de integração, sobre o
PostgreSQL provisionado por Testcontainers.

## Desvantagens

- Migration aplicada é imutável: corrigir um script já executado exige uma migration nova,
  e nunca a edição do arquivo anterior, sob pena de quebrar a validação de checksum do
  Flyway. Isso pega de surpresa quem ainda não trabalhou com a ferramenta.
- Toda alteração de entidade passa a exigir o trabalho manual de escrever o SQL
  correspondente, onde antes bastava mudar a anotação.
- O perfil local com `ddl-auto: none` não valida o mapeamento, então uma divergência entre
  entidade e schema só aparece ao subir a aplicação em Docker ou no cluster.
- Os scripts são escritos em SQL específico do PostgreSQL, o que amarra as migrations a
  esse banco.
- Migrations desenvolvidas em paralelo podem colidir na numeração, exigindo coordenação
  entre as pessoas do grupo ou renumeração no momento do merge.

## Alternativas consideradas

- **Deixar o Hibernate gerar o schema** com `ddl-auto: update`. Não custa nada para
  começar, mas o resultado depende do estado anterior de cada banco, não versiona a
  mudança, não tem como expressar carga de dados nem transformação de dados existentes, e
  não oferece caminho de rollback. Em produção é arriscado: uma alteração de mapeamento
  pode implicar uma mudança destrutiva sem que ninguém tenha revisado.
- **Scripts SQL aplicados manualmente.** Dão controle total sobre o DDL, mas dependem de
  disciplina para saber o que já rodou em cada ambiente, e um script esquecido só aparece
  como erro em tempo de execução. Com quatro ambientes e quatro pessoas, a chance de
  divergência é alta.
- **Liquibase.** Resolve o mesmo problema que o Flyway, com changelogs em XML, YAML ou
  JSON e recursos de rollback declarativo. A camada de abstração afasta quem já sabe SQL e
  acrescenta um formato a aprender. O Flyway usa SQL puro e tem integração direta com o
  Spring Boot.
- **Dump versionado do schema completo**, em vez de migrations incrementais. Simples de
  recriar do zero, mas não expressa transformação de dados existentes e torna impossível
  evoluir um banco que já tem conteúdo.

## Questões em aberto

- Como evitar colisão de numeração entre migrations desenvolvidas em paralelo? Convenção
  de faixas por pessoa, ou renumeração no merge?
- Os dados de carga inicial devem ficar nas mesmas migrations da estrutura, ou separados
  em scripts próprios? Separar facilita distinguir schema de conteúdo, mas cria duas
  sequências para acompanhar.
- Vale usar `ddl-auto: validate` também no perfil local, para pegar divergências mais
  cedo?
- Migrations que alterem a tabela `customers` podem quebrar a Lambda de autenticação, que
  a lê diretamente. Como sinalizar isso na revisão?

## Possibilidades futuras

- Acrescentar verificação no CI de que as migrations rodam do zero em banco limpo, se a
  suíte de integração deixar de cobrir esse caminho.
- Adotar migrations repetíveis (`R__`) para views ou funções, caso venham a existir.
- Estabelecer um procedimento de revisão específico para migrations destrutivas, como
  remoção de coluna.

## Decisão registrada

- **Resultado**: Aceita
- **Data**: 2026-09-06
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**: [ADR-0007](../adr/0007-versionar-o-schema-com-flyway.md)

O ponto decisivo foi tirar a mudança de schema do território do efeito colateral: com
Flyway, ela passa pela revisão de Pull Request como qualquer outro código, e com
`validate` uma divergência entre entidade e tabela derruba a aplicação na inicialização em
vez de virar erro obscuro na primeira consulta.

As questões em aberto foram resolvidas assim: a colisão de numeração é tratada no merge,
por ser rara com o volume atual; os dados de carga inicial ficam nas mesmas migrations da
estrutura; o perfil local permanece com `ddl-auto: none`, com a validação delegada aos
ambientes conteinerizados; e o acoplamento da Lambda à tabela `customers` foi registrado
como consequência negativa na
[RFC-0004](0004-autenticar-clientes-sem-cadastro-de-senha.md), a ser tratado quando
incomodar.
