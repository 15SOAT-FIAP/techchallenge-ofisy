# 0007. Versionar o schema do banco com Flyway

Data: 2026-09-06

## Status

Aceito

## Contexto

O schema do banco evolui junto com a aplicação: novas tabelas por agregado, colunas
acrescentadas a entidades existentes e dados de carga inicial, como usuários e catálogo de
serviços. Essa evolução acontece em paralelo por várias pessoas do grupo e precisa chegar de
forma idêntica ao ambiente local, ao contêiner de desenvolvimento, ao banco dos testes de
integração e ao RDS de produção.

As opções avaliadas:

- **Deixar o Hibernate gerar o schema** com `ddl-auto: update`. Não custa nada para começar,
  mas o resultado depende do estado anterior de cada banco, não versiona a mudança, não tem
  como expressar carga de dados nem transformação de dados existentes, e não oferece caminho
  de rollback. Em produção é arriscado: uma alteração de mapeamento pode implicar uma
  mudança destrutiva sem que ninguém tenha revisado.
- **Scripts SQL aplicados manualmente.** Dão controle total sobre o DDL, mas dependem de
  disciplina para saber o que já rodou em cada ambiente, e um script esquecido só aparece
  como erro em tempo de execução.
- **Ferramenta de migration versionada**, com histórico do que já foi aplicado. Flyway e
  Liquibase resolvem o mesmo problema; o Flyway usa SQL puro, sem uma camada de abstração em
  XML ou YAML, e tem integração direta com o Spring Boot.

## Decisão

Vamos versionar o schema com Flyway, em scripts SQL numerados sob
`src/main/resources/db/migration`, seguindo a convenção `V<n>__descricao.sql`.

O Hibernate não altera o schema em nenhum ambiente. Nos perfis `docker` e `k8s` a
configuração é `ddl-auto: validate`, o que faz a aplicação conferir na inicialização se o
mapeamento das entidades corresponde às tabelas existentes e falhar caso divirjam. No perfil
de desenvolvimento local a configuração é `ddl-auto: none`, deixando a validação para os
ambientes conteinerizados.

As migrations cobrem tanto estrutura quanto dados de carga inicial, incluindo os usuários
usados na avaliação do projeto. Elas também rodam nos testes de integração, sobre o
PostgreSQL provisionado por Testcontainers, conforme o
[ADR-0008](0008-testar-integracao-com-testcontainers.md).

## Consequências

- (+) Todo ambiente converge para o mesmo schema a partir do mesmo conjunto de scripts,
  aplicados na mesma ordem.
- (+) A mudança de banco passa pela revisão de Pull Request como qualquer outro código, em
  vez de acontecer como efeito colateral de uma anotação JPA.
- (+) Com `validate`, uma divergência entre entidade e tabela derruba a aplicação na
  inicialização, em vez de virar erro obscuro na primeira consulta que usar a coluna
  ausente.
- (+) Os dados de carga inicial fazem parte do versionamento, o que torna o ambiente de
  avaliação reproduzível.
- (-) Migration aplicada é imutável: corrigir um script já executado exige uma migration
  nova, e nunca a edição do arquivo anterior, sob pena de quebrar a validação de checksum do
  Flyway.
- (-) Toda alteração de entidade passa a exigir o trabalho manual de escrever o SQL
  correspondente.
- (-) O perfil local com `ddl-auto: none` não valida o mapeamento, então uma divergência
  entre entidade e schema só aparece ao subir a aplicação em Docker ou no cluster.
- (-) Os scripts são escritos em SQL específico do PostgreSQL, o que amarra as migrations a
  esse banco.