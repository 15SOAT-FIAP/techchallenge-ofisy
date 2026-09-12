# 0006. Separar a autenticação de clientes e de funcionários

Data: 2026-09-06

## Status

Aceito

## Contexto

O sistema tem dois públicos com necessidades de acesso muito diferentes.

Os **funcionários** da oficina operam o sistema inteiro e precisam de permissões
distintas entre si: um mecânico não faz o que um administrador faz. São usuários
cadastrados, com senha, e o controle é por papel (`ADMIN`, `ATTENDANT`, `MECHANIC`,
`STOCKMAN`).

Os **clientes** acessam um recorte pequeno: consultar o status da própria ordem de
serviço, aprovar ou reprovar um orçamento e ler as notificações que lhes dizem respeito.
Exigir cadastro de senha para isso seria atrito desnecessário, e a Fase 3 pede
explicitamente autenticação de cliente por CPF, implementada como function serverless.

Antes da Fase 3 esses endpoints de cliente eram públicos no monolito, o que não é
aceitável para dados de ordem de serviço.

As opções avaliadas para a autenticação de clientes:

- **Implementar no próprio monolito**, com mais um filtro no Spring Security. Reaproveita
  o que já existe, mas contraria o requisito de solução serverless e mantém a validação de
  token de cliente acoplada ao ciclo de deploy da aplicação principal.
- **Function serverless separada, com validação do token pela aplicação.** Atende o
  requisito, mas cada requisição de cliente ainda chegaria ao monolito para só então ser
  rejeitada, gastando recurso do cluster com tráfego não autorizado.
- **Function serverless separada, com validação do token na borda**, por um Lambda
  Authorizer acionado pelo API Gateway antes de qualquer roteamento para o cluster.

## Decisão

Vamos manter dois fluxos de autenticação independentes, com tecnologias e ciclos de vida
próprios.

**Funcionários**, nesta aplicação: autenticação por usuário e senha em `/api/v1/login`,
sessão `STATELESS` no Spring Security, JWT validado por um filtro próprio e autorização por
papel declarada com `@PreAuthorize` em cada método de controller. Por não haver sessão em
servidor, a proteção CSRF fica desabilitada de forma deliberada.

**Clientes**, no repositório `techchallenge-ofisy-auth`: duas Lambdas em Go, empacotadas
como `provided.al2023` em arm64.

- A Lambda de **autenticação** atende `POST /auth/customers`, valida o formato do CPF ou
  CNPJ, consulta o cliente diretamente no PostgreSQL, verifica se está ativo e devolve um
  JWT assinado em HS256, com o id do cliente no `subject` e emissor
  `techchallenge-ofisy-auth`. Ela não chama esta aplicação: fala com o banco por conta
  própria.
- A Lambda **authorizer** é um Lambda Authorizer do tipo `REQUEST` com payload 2.0 e
  respostas simples. O API Gateway a invoca antes de rotear, ela valida assinatura,
  expiração e emissor do token e responde apenas `isAuthorized`, propagando o `customerId`
  no contexto. Qualquer motivo de recusa vira uma negativa genérica, e a causa real fica só
  nos logs.

O **API Gateway HTTP** é o único ponto de entrada público, já que o NLB do cluster é
interno. As rotas de cliente são declaradas uma a uma com `authorization_type = CUSTOM`
apontando para o authorizer, e alcançam a aplicação por VPC Link sobre as subnets privadas.

Ambos os lados assinam e validam com HS256 sobre o mesmo segredo compartilhado.

## Consequências

- (+) Cada público autentica pelo meio adequado: o cliente não cria senha, e o funcionário
  mantém autorização granular por papel.
- (+) Requisição de cliente com token inválido é barrada na borda, no API Gateway, e nunca
  consome recurso do cluster.
- (+) A autenticação de cliente escala e é cobrada por invocação, independente do
  dimensionamento do EKS, e tem ciclo de deploy próprio.
- (+) Os endpoints de cliente, antes públicos, passaram a ser protegidos sem alteração no
  código da aplicação principal.
- (+) O NLB interno somado ao API Gateway elimina a exposição direta do cluster à internet.
- (-) O JWT usa HS256, algoritmo simétrico: o mesmo segredo assina e valida, e precisa ser
  distribuído entre a Lambda e a aplicação. Um algoritmo assimétrico como RS256 permitiria
  publicar apenas a chave pública para quem valida, ao custo de gerenciar um par de chaves.
- (-) O segredo compartilhado cria acoplamento operacional entre repositórios: rotacioná-lo
  exige atualizar a Lambda e a aplicação de forma coordenada.
- (-) Toda rota de cliente é declarada individualmente no Terraform do API Gateway. Um
  endpoint novo para cliente só fica protegido depois de acrescentado lá, e esquecer disso o
  deixa cair na rota `ANY /{proxy+}`, que não tem authorizer.
- (-) A Lambda de autenticação lê a tabela `customers` diretamente, o que a acopla ao
  esquema do banco da aplicação: uma migration nessa tabela pode quebrá-la sem que nada
  neste repositório acuse o problema.
- (-) A regra de autenticação de cliente passa a viver em Go, em outro repositório, exigindo
  do time familiaridade com duas linguagens.