# 0001. Registrar decisões de arquitetura

Data: 2026-09-05

## Status

Aceito

## Contexto

O Ofisy está distribuído em quatro repositórios independentes: a aplicação Spring Boot,
a função Lambda de autenticação de clientes, a infraestrutura do cluster EKS e a
infraestrutura do banco RDS, ambas em Terraform. O projeto é desenvolvido por um grupo,
ao longo de várias fases, e as decisões de arquitetura tomadas em uma fase precisam ser
compreendidas nas seguintes.

Hoje as justificativas das escolhas técnicas existem, mas estão espalhadas: parte nas
tabelas de stack e dependências do `README.md`, parte na prosa dos guias em `docs/` e
parte apenas no histórico de discussão dos Pull Requests. Nenhum desses lugares registra
a data da decisão, seu status atual ou as alternativas que foram avaliadas e descartadas.
Recuperar por que algo foi feito de determinada maneira, e principalmente o que foi
recusado no caminho, é caro.

Alternativas consideradas:

- Continuar concentrando as justificativas no `README.md`. É o caminho de menor esforço,
  mas o arquivo já passa de duzentas linhas, não versiona a decisão como unidade própria
  e não tem como expressar que uma escolha foi superada por outra.
- Usar a wiki do GitHub. Afasta a decisão do commit que a implementa, fica fora do fluxo
  de revisão por Pull Request e não acompanha o histórico do código.
- Adotar Architecture Decision Records em Markdown versionado junto ao código, como
  descrito por Michael Nygard. A decisão passa a viver no mesmo repositório da
  implementação, é revisada no mesmo Pull Request e ganha status e data explícitos.

## Decisão

Vamos registrar as decisões de arquitetura do projeto como ADRs em `docs/adr/`, no
formato Nygard, escritos em português e versionados junto ao código.

Cada ADR é um arquivo nomeado `NNNN-titulo-em-kebab-case.md`, com numeração sequencial a
partir de `0001`. Um ADR aceito não é reescrito: quando a decisão muda, cria-se um novo
ADR e altera-se somente a linha de Status do anterior, apontando para o sucessor.

O processo completo, incluindo o ciclo de vida dos status e o critério para decidir o que
merece um ADR, está descrito no [README do diretório](README.md).

## Consequências

- (+) A motivação de cada escolha arquitetural passa a ter um lugar único, datado e
  versionado, acessível a partir do próprio repositório.
- (+) As alternativas descartadas ficam registradas, evitando que o time volte a discutir
  uma opção que já foi avaliada e recusada.
- (+) A revisão da decisão acontece no mesmo Pull Request que a implementa.
- (+) O conjunto de ADRs serve como material direto para a avaliação da Fase 3.
- (-) Toda decisão arquitetural relevante passa a exigir disciplina: escrever um arquivo
  novo e atualizar a tabela-índice do `README.md` do diretório.
- (-) As decisões já tomadas nas fases anteriores ficam sem registro até que os ADRs
  retroativos sejam escritos, trabalho que ainda precisa ser feito.