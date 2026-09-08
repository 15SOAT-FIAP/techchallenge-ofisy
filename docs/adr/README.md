# Architecture Decision Records (ADRs)

Um ADR registra uma decisão de arquitetura relevante do projeto: o contexto que a
motivou, as alternativas avaliadas, o que foi decidido e as consequências assumidas.
O objetivo é preservar o **porquê** das escolhas, que os guias operacionais deste
diretório não contam, e deixar rastro do que foi descartado no caminho.

A adoção dos ADRs neste projeto está registrada no
[ADR-0001](0001-registrar-decisoes-de-arquitetura.md).

## Índice

| ADR                                                                  | Título                                               | Status | Data       |
|----------------------------------------------------------------------|------------------------------------------------------|--------|------------|
| [0001](0001-registrar-decisoes-de-arquitetura.md)                    | Registrar decisões de arquitetura                    | Aceito | 2026-09-05 |
| [0002](0002-adotar-clean-architecture-com-ddd.md)                    | Adotar Clean Architecture com DDD                    | Aceito | 2026-09-06 |
| [0003](0003-expor-a-aplicacao-por-api-rest.md)                       | Expor a aplicação por API REST                       | Aceito | 2026-09-06 |
| [0004](0004-utilizar-a-aws-como-provedor-de-nuvem.md)                | Utilizar a AWS como provedor de nuvem                | Aceito | 2026-09-06 |
| [0005](0005-escalar-a-aplicacao-com-hpa-por-cpu.md)                  | Escalar a aplicação com HPA por CPU                  | Aceito | 2026-09-06 |
| [0006](0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md) | Separar a autenticação de clientes e de funcionários | Aceito | 2026-09-06 |
| [0007](0007-versionar-o-schema-com-flyway.md)                        | Versionar o schema do banco com Flyway               | Aceito | 2026-09-06 |
| [0008](0008-testar-integracao-com-testcontainers.md)                 | Testar a integração com Testcontainers               | Aceito | 2026-09-06 |
| [0009](0009-registrar-notificacoes-de-forma-sincrona-no-banco.md)    | Registrar notificações de forma síncrona no banco    | Aceito | 2026-09-06 |

## Formato

Os ADRs seguem o formato proposto por Michael Nygard, com as seções **Status**,
**Contexto**, **Decisão** e **Consequências**. O modelo a ser copiado está em
[`template.md`](template.md).

Nas **Consequências**, cada item é prefixado com `(+)` quando é positivo e `(-)` quando
é negativo, deixando explícitos os custos assumidos junto com os ganhos.

As alternativas descartadas são descritas dentro da seção **Contexto**. O formato Nygard
não tem uma seção própria para elas, e é importante que não se percam.

## Convenção de nomes

Cada ADR é um arquivo `NNNN-titulo-em-kebab-case.md`, com numeração sequencial de quatro
dígitos a partir de `0001`.

Números não são reaproveitados. Se um ADR for descontinuado ou substituído, o arquivo
permanece no diretório com o status atualizado e o número morre com ele.

## Ciclo de vida do status

```
Proposto  ->  Aceito  ->  Substituído por ADR-XXXX
                      ->  Descontinuado
```

- **Proposto**: decisão em discussão, ainda em revisão no Pull Request.
- **Aceito**: decisão vigente.
- **Substituído por ADR-XXXX**: outra decisão tomou o lugar desta.
- **Descontinuado**: a decisão deixou de valer e nada a substituiu.

Um ADR aceito é imutável. Se a decisão mudar, escreva um ADR novo e edite apenas a linha
de Status do antigo, com o link para o sucessor. Reescrever a decisão original apaga o
histórico, que é justamente o que o registro existe para guardar.

## Quando escrever um ADR

Escreva um ADR quando a decisão:

- for cara de reverter depois de implementada;
- afetar mais de um módulo ou mais de um dos repositórios do projeto;
- ou for previsivelmente questionada meses depois, por quem não participou da discussão.

A escolha de uma biblioteca utilitária pontual, restrita a um ponto do código e trocável
sem impacto, não precisa de ADR.

## Como criar um ADR

1. Copie [`template.md`](template.md) para `NNNN-titulo-em-kebab-case.md`, usando o
   próximo número livre do índice acima.
2. Preencha as seções, começando com o status `Proposto`.
3. Acrescente a linha correspondente na tabela do índice.
4. Abra um Pull Request, marque o impacto **Aplicação** no template de PR e referencie a
   issue relacionada.
5. Após a aprovação, altere o status para `Aceito`.