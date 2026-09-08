# 0008. Testar a integração com Testcontainers

Data: 2026-09-06

## Status

Aceito

## Contexto

A maior parte da suíte é de testes unitários, que exercitam regra de negócio com dependências
mockadas e não tocam em banco. Isso cobre o domínio e os casos de uso, mas deixa de fora
justamente o que só quebra na fronteira: o mapeamento JPA das entidades, as consultas dos
gateways, as migrations do Flyway e o comportamento transacional.

As opções avaliadas para os testes que precisam de banco:

- **Banco em memória, como H2 em modo de compatibilidade com PostgreSQL.** Sobe rápido e não
  exige nada instalado, mas não é PostgreSQL: tipos, funções e detalhes de dialeto divergem.
  Um teste verde no H2 não garante que a consulta funcione em produção, e as migrations
  escritas em SQL do PostgreSQL podem simplesmente não rodar. O risco é o pior possível: uma
  suíte que passa e esconde o defeito.
- **Banco compartilhado de desenvolvimento.** É o banco real, mas o estado é compartilhado
  entre quem estiver rodando os testes ao mesmo tempo, os resultados ficam dependentes de
  ordem, e a execução na pipeline exigiria conectividade e credenciais.
- **Testcontainers**, subindo um PostgreSQL descartável em Docker durante a execução dos
  testes. É o banco real, isolado por execução, sem depender de infraestrutura externa. O
  custo é exigir Docker disponível e somar o tempo de inicialização do contêiner.

## Decisão

Vamos executar os testes de integração contra um PostgreSQL real, provisionado por
Testcontainers, com REST Assured para exercitar a API pela porta HTTP.

A infraestrutura fica concentrada em `IntegrationTestBase`, classe abstrata que as classes de
teste de integração estendem. Ela sobe um `PostgreSQLContainer` na imagem `postgres:16`, a
mesma versão usada em desenvolvimento, e injeta url, usuário e senha via
`@DynamicPropertySource`.

O contêiner é um campo estático iniciado uma única vez, reaproveitado por todas as classes de
teste da execução, em vez de recriado a cada uma. O isolamento entre testes vem da limpeza das
tabelas em `@AfterEach`, e não da recriação do banco.

A aplicação sobe em porta aleatória com `SpringBootTest`, e o Flyway aplica as migrations
sobre esse banco, o que faz de cada execução também uma verificação de que os scripts rodam do
zero.

Por convenção, testes de integração terminam em `IT` e testes unitários em `Test`.

## Consequências

- (+) O mapeamento JPA, as consultas dos gateways e as migrations são exercitados contra o
  mesmo banco que roda em produção, no mesmo major version.
- (+) As migrations são validadas do zero a cada execução da suíte, o que faz um script
  inválido falhar no CI e não no deploy.
- (+) Os testes de controller passam pela pilha real de HTTP e de segurança, incluindo a
  obtenção de token por `/api/v1/login`.
- (+) Cada execução parte de um banco próprio e descartável, sem depender de infraestrutura
  compartilhada nem deixar resíduo.
- (-) Docker passa a ser pré-requisito para rodar a suíte, tanto na máquina de quem
  desenvolve quanto no runner do CI.
- (-) A suíte fica mais lenta: subir o contêiner e o contexto do Spring custa
  significativamente mais que um teste unitário.
- (-) Reaproveitar um único contêiner entre classes torna o isolamento dependente da limpeza
  em `@AfterEach`. Uma tabela nova que não seja acrescentada a essa limpeza vaza estado entre
  testes, com falhas sensíveis à ordem de execução.
- (-) O custo por teste desestimula escrever muitos testes de integração, o que torna a
  escolha do que merece esse nível uma decisão deliberada.