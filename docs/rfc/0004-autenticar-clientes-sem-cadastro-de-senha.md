# RFC-0004. Autenticar clientes sem cadastro de senha

Data: 05/09/2026
Autor: @rogerbertan

## Status

Encerrada - Aprovada

## Resumo

Manter dois fluxos de autenticação independentes: funcionários seguem nesta aplicação por
usuário e senha, e clientes vão para uma function serverless separada, com validação do
token na borda por um Lambda Authorizer no API Gateway.

## Problema

O sistema tem dois públicos com necessidades de acesso muito diferentes.

Os **funcionários** da oficina operam o sistema inteiro e precisam de permissões distintas
entre si: um mecânico não faz o que um administrador faz. São usuários cadastrados, com
senha, e o controle é por papel (`ADMIN`, `ATTENDANT`, `MECHANIC`, `STOCKMAN`).

Os **clientes** acessam um recorte pequeno: consultar o status da própria ordem de
serviço, aprovar ou reprovar um orçamento e ler as notificações que lhes dizem respeito.
Exigir cadastro de senha para isso seria atrito desnecessário, e a Fase 3 pede
explicitamente autenticação de cliente por CPF, implementada como function serverless.

Há um problema aberto agora. Antes da Fase 3 esses endpoints de cliente eram públicos no
monolito, o que não é aceitável para dados de ordem de serviço: qualquer pessoa que
descubra o identificador de uma ordem consegue lê-la. Isso precisa ser fechado, e a forma
de fechar determina o desenho da autenticação de clientes.

## Proposta Técnica

Manter dois fluxos de autenticação independentes, com tecnologias e ciclos de vida
próprios.

**Funcionários**, nesta aplicação: autenticação por usuário e senha em `/api/v1/login`,
sessão `STATELESS` no Spring Security, JWT validado por um filtro próprio e autorização
por papel declarada com `@PreAuthorize` em cada método de controller. Por não haver sessão
em servidor, a proteção CSRF fica desabilitada de forma deliberada.

**Clientes**, no repositório `techchallenge-ofisy-auth`: duas Lambdas em Go, empacotadas
como `provided.al2023` em arm64.

A Lambda de **autenticação** atende `POST /auth/customers`, valida o formato do CPF ou
CNPJ, consulta o cliente diretamente no PostgreSQL, verifica se está ativo e devolve um
JWT assinado em HS256, com o id do cliente no `subject` e emissor
`techchallenge-ofisy-auth`. Ela não chama esta aplicação: fala com o banco por conta
própria.

A Lambda **authorizer** é um Lambda Authorizer do tipo `REQUEST` com payload 2.0 e
respostas simples. O API Gateway a invoca antes de rotear, ela valida assinatura,
expiração e emissor do token e responde apenas `isAuthorized`, propagando o `customerId`
no contexto. Qualquer motivo de recusa vira uma negativa genérica, e a causa real fica só
nos logs.

**Entrada.** O API Gateway HTTP é o único ponto de entrada público, já que o NLB do
cluster é interno. As rotas de cliente são declaradas uma a uma com `authorization_type =
CUSTOM` apontando para o authorizer, e alcançam a aplicação por VPC Link sobre as subnets
privadas.

Ambos os lados assinam e validam com HS256 sobre o mesmo segredo compartilhado.

## Impacto esperado

**Benefícios.**

- Os endpoints de ordem de serviço, hoje públicos no monolito, passam a ser protegidos sem
  alteração no código da aplicação principal.
- A validação acontece na borda: requisição de cliente com token inválido é barrada no API
  Gateway e nunca consome recurso do cluster.
- O cliente acessa o próprio recorte sem cadastrar senha, eliminando o atrito que a fase
  quis evitar.
- Os dois fluxos têm ciclos de deploy independentes, de modo que mudar a autenticação de
  cliente não exige redeploy da aplicação principal.
- O authorizer poderá ser reaproveitado pelos serviços extraídos na Fase 4, que precisarão
  validar o token de cliente por conta própria.

**Riscos e custos.**

- O JWT usa HS256, algoritmo simétrico: o mesmo segredo assina e valida, e precisa ser
  distribuído entre a Lambda e a aplicação. Um algoritmo assimétrico como RS256 permitiria
  publicar apenas a chave pública para quem valida, ao custo de gerenciar um par de
  chaves.
- O segredo compartilhado cria acoplamento operacional entre repositórios: rotacioná-lo
  exige atualizar a Lambda e a aplicação de forma coordenada. Migrar para RS256, com chave
  privada apenas no emissor, e publicar um endpoint JWKS eliminaria o segredo compartilhado
  e permitiria rotação sem redeploy coordenado.
- Toda rota de cliente é declarada individualmente no Terraform do API Gateway. Um
  endpoint novo para cliente só fica protegido depois de acrescentado lá, e esquecer disso
  o deixa cair na rota `ANY /{proxy+}`, que não tem authorizer. A falha é silenciosa: o
  endpoint funciona, apenas sem proteção.
- A Lambda de autenticação lê a tabela `customers` diretamente, o que a acopla ao esquema
  do banco da aplicação. Uma migration nessa tabela pode quebrá-la sem que nada neste
  repositório acuse o problema.
- A regra de autenticação de cliente passa a viver em Go, em outro repositório, exigindo
  do time familiaridade com duas linguagens.
- Autenticar por CPF sem segredo algum significa que conhecer o CPF de alguém basta para
  acessar as ordens de serviço dessa pessoa. É o que a fase pede, mas é uma credencial
  fraca por natureza. Um segundo fator, como código enviado por e-mail, é o caminho caso
  ela se mostre fraca demais.

## Alternativas consideradas

- **Implementar no próprio monolito**, com mais um filtro no Spring Security. Reaproveita
  o que já existe e mantém tudo em uma linguagem, mas contraria o requisito de solução
  serverless da Fase 3 e mantém a validação de token de cliente acoplada ao ciclo de
  deploy da aplicação principal.
- **Function serverless separada, com validação do token pela aplicação.** Atende o
  requisito de serverless, mas cada requisição de cliente ainda chegaria ao monolito para
  só então ser rejeitada, gastando recurso do cluster com tráfego não autorizado.
- **Cognito como provedor de identidade.** Elimina a gestão de token e de chaves por
  completo e traz fluxos prontos. Descartada porque a Fase 3 pede explicitamente uma
  function serverless de autenticação, e porque login por CPF sem senha não é o caso de
  uso natural do Cognito, exigindo adaptação que anularia boa parte do ganho.
- **Manter os endpoints de cliente públicos.** Nenhum trabalho, e é o estado atual.
  Inaceitável: expõe dados de ordem de serviço de qualquer cliente a quem descobrir o
  identificador.

## Pontos em aberto

- HS256 resolve para esta fase, mas o segredo compartilhado entre dois repositórios é
  dívida assumida. Vale já adotar RS256, ou fica para depois de a Fase 3 ser entregue?
- Como evitar que um endpoint de cliente novo nasça desprotegido por esquecimento no
  Terraform? Há como inverter o padrão, de modo que o não declarado seja negado?
- A Lambda lendo `customers` direto do banco é aceitável, ou deveria consultar a aplicação
  por um endpoint interno? O endpoint acopla ao deploy do monolito, que é o que se quis
  evitar.
- O token de cliente deve ter expiração mais curta que o de funcionário, dado que a
  credencial de entrada é apenas o CPF?

## Decisão registrada

- **Resultado**: Encerrada - Aprovada
- **Data**: 06/09/2026
- **Aprovada por**: @binhajus, @kalelfleith, @Tetheugas
- **ADR gerado**:
  [ADR-0006](../adr/0006-separar-a-autenticacao-de-clientes-e-de-funcionarios.md)

Validar na borda foi o ponto decisivo: requisição de cliente com token inválido é barrada
no API Gateway e nunca consome recurso do cluster, e os endpoints antes públicos passaram
a ser protegidos sem alteração no código da aplicação principal.

As questões em aberto foram resolvidas assim: HS256 fica para esta fase, com a migração
para RS256 registrada como dívida no ADR; a proteção de rotas continua declarada
individualmente no Terraform, com a falha silenciosa assumida como consequência negativa;
a Lambda segue lendo `customers` direto do banco, para não acoplar ao deploy do monolito;
e a expiração do token de cliente ficou igual à do de funcionário, por simplicidade.
