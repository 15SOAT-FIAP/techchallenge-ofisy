# Requests for Comments (RFCs)

Uma RFC propõe uma mudança relevante e a coloca em discussão **antes** de ela ser
implementada. O documento apresenta o problema, o desenho proposto, o que ele custa e o
que ficou em aberto, para que o time possa concordar, discordar ou sugerir outro caminho
enquanto ainda é barato mudar de ideia.

## RFC ou ADR?

Os dois instrumentos são complementares e se distinguem pelo momento:

|              | RFC                                 | ADR                          |
|--------------|-------------------------------------|------------------------------|
| **Momento**  | Antes da decisão                    | Depois da decisão            |
| **Pergunta** | "Devemos fazer isso?"               | "O que decidimos e por quê?" |
| **Estado**   | Aberta a mudanças durante a revisão | Imutável após aceita         |
| **Objetivo** | Coletar feedback                    | Preservar o histórico        |

Uma RFC aceita normalmente **gera um ADR**, que registra a decisão em definitivo. A RFC
guarda o debate; o ADR guarda o resultado.

Quando a decisão já está tomada e não há alternativas em aberto, escreva direto um ADR.
Uma RFC que ninguém pode contestar é burocracia. Os ADRs deste projeto estão em
[`docs/adr`](../adr/README.md).

## Índice

| RFC                                                       | Título                                    | Status               | Data       | ADR gerado                                                                      |
|-----------------------------------------------------------|-------------------------------------------|----------------------|------------|---------------------------------------------------------------------------------|
| [0001](0001-registrar-e-entregar-notificacoes.md)         | Registrar e entregar notificações         | Encerrada - Aprovada | 04/09/2026 | [ADR-0009](../adr/0009-registrar-notificacoes-de-forma-sincrona-no-banco.md)    |
| [0002](0002-organizar-o-codigo-da-aplicacao.md)           | Organizar o código da aplicação           | Encerrada - Aprovada | 03/09/2026 | [ADR-0002](../adr/0002-adotar-clean-architecture-com-ddd.md)                    |
| [0003](0003-definir-o-estilo-de-comunicacao-da-api.md)    | Definir o estilo de comunicação da API    | Encerrada - Aprovada | 03/09/2026 | [ADR-0003](../adr/0003-expor-a-aplicacao-por-api-rest.md)                       |
| [0004](0004-autenticar-clientes-sem-cadastro-de-senha.md) | Autenticar clientes sem cadastro de senha | Encerrada - Aprovada | 05/09/2026 | [ADR-0006](../adr/0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md) |
| [0005](0005-escalar-a-aplicacao-no-cluster.md)            | Escalar a aplicação no cluster            | Encerrada - Aprovada | 05/09/2026 | [ADR-0005](../adr/0005-escalar-a-aplicacao-com-hpa-por-cpu.md)                  |
| [0006](0006-versionar-a-evolucao-do-schema.md)            | Versionar a evolução do schema            | Encerrada - Aprovada | 04/09/2026 | [ADR-0007](../adr/0007-versionar-o-schema-com-flyway.md)                        |

## Formato

As RFCs seguem o formato apresentado na disciplina, com as seções **Resumo**,
**Problema**, **Proposta Técnica**, **Impacto esperado**, **Alternativas consideradas** e
**Pontos em aberto**. O modelo a ser copiado está em [`template.md`](template.md).

A ele o projeto acrescenta duas seções:

- **Autor**, no cabeçalho, para saber a quem dirigir as dúvidas durante a revisão.
- **Decisão registrada**, ao final, que fecha o ciclo apontando o ADR nascido da proposta.

Duas seções carregam o peso da revisão e não podem ser tratadas como formalidade:

- **Impacto esperado** cobre benefícios e custos. A parte de riscos e custos é obrigatória
  e não aceita "nenhum": toda proposta tem custo, e não enxergá-lo é sinal de análise
  incompleta.
- **Pontos em aberto** deve trazer perguntas concretas. É ali que o autor dirige a
  revisão para os pontos em que realmente precisa de outra opinião.

A seção **Decisão registrada** é preenchida ao final e fecha o ciclo, apontando o ADR
que nasceu da proposta.

## Convenção de nomes

Cada RFC é um arquivo `NNNN-titulo-em-kebab-case.md`, com numeração sequencial de quatro
dígitos a partir de `0001`.

A numeração é independente da dos ADRs: a RFC-0001 não tem relação com o ADR-0001.

Números não são reaproveitados. Uma RFC rejeitada ou cancelada permanece no diretório com o
status atualizado, porque saber o que foi recusado e por quê é tão útil quanto saber o
que foi aceito.

## Ciclo de vida do status

```
Rascunho  ->  Aberta para comentários  ->  Revisada  ->  Encerrada - Aprovada   ->  gera um ADR
                                                     ->  Encerrada - Rejeitada
                                                     ->  Cancelada
```

- **Rascunho**: documento inicial, ainda sem revisão da equipe.
- **Aberta para comentários**: em discussão ativa pelo time.
- **Revisada**: atualizada com base nos feedbacks.
- **Encerrada - Aprovada**: proposta aprovada. Segue para implementação e origina um ADR.
- **Encerrada - Rejeitada**: proposta descartada, com justificativa registrada. O
  documento fica como registro do que não foi seguido.
- **Cancelada**: interrompida por falta de relevância, mudança de escopo ou substituição.
  É também o status de uma proposta que faz sentido, mas não agora.

## Processo de revisão

O time tem quatro desenvolvedores, e a revisão acontece nos comentários do Pull Request:

1. O autor abre o PR com a RFC no status `Rascunho`.
2. O período de comentários é de **72 horas** a partir da abertura do PR. Ao abrir a
   discussão, o status passa a `Aberta para comentários`; havendo ajustes no texto a
   partir dos feedbacks, passa a `Revisada`.
3. A RFC passa a `Encerrada - Aprovada` com **duas aprovações** além do autor.
4. Encerrado o prazo, decide-se com quem se manifestou. Uma RFC não fica aberta
   indefinidamente à espera de quorum.

O prazo existe para proteger contra o modo de falha mais comum em time pequeno: ninguém
aprova, ninguém rejeita, e a proposta morre de indefinição.

## Quando escrever uma RFC

Escreva uma RFC quando a mudança:

- afetar o trabalho dos outros integrantes do time, obrigando-os a mudar o que estão
  fazendo;
- tiver mais de um caminho técnico defensável, em que a escolha não é óbvia;
- ou for cara de reverter depois de implementada.

Mudança localizada, reversível e de caminho único não precisa de RFC. Corrigir um bug,
extrair um método ou acrescentar um campo a um DTO existente entram direto no Pull
Request comum.

## Como criar uma RFC

1. Copie [`template.md`](template.md) para `NNNN-titulo-em-kebab-case.md`, usando o
   próximo número livre do índice acima.
2. Preencha as seções, começando com o status `Rascunho`.
3. Acrescente a linha correspondente na tabela do índice.
4. Abra um Pull Request, marque os impactos no template de PR e referencie a issue
   relacionada.
5. Conduza a discussão nos comentários, atualizando o texto conforme o feedback.
6. Ao final, preencha a seção **Decisão registrada** e atualize o status no documento e
   no índice.
7. Se a RFC foi aceita, escreva o ADR correspondente em [`docs/adr`](../adr/README.md) e
   aponte-o na **Decisão registrada**.