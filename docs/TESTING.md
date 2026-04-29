# Guia de Testes e Análises

## Análise de cobertura de testes e vulnerabilidades com SonarQube

Este guia apresenta como configurar e executar o SonarQube localmente para analisar a cobertura de testes e possíveis vulnerabilidades da aplicação.

---

### Primeiro Acesso

#### 1. Subir o container do SonarQube

```bash
docker compose -f compose.sonar.yaml up -d
```

#### 2. Acessar a interface web do SonarQube

1. Abra o navegador:
- Acesse: http://localhost:9000

2. Faça login com usuário e senha padrão:
- Usuário: **admin**
- Senha: **admin**

#### 3. Alterar senha inicial

No primeiro acesso, o SonarQube solicitará a alteração da senha padrão.

#### 4. Ajustar permissões e opções de autenticação para análise local
1. Acesse `Administration` > `General Settings` > `Security` (ou acesse diretamente http://localhost:9000/admin/settings?category=security)
   1. No menu lateral da esquerda, clique em `Security`
   2. Desative a opção `Force user authentication`
2. Acesse `Administration` > `Global permissions` (ou acesse diretamente http://localhost:9000/admin/permissions)
   1. Localize o grupo de usuários `Anyone`
   2. Marque as opções `Execute Analysis` e `Create`

#### 5. Executar análise

No diretório raiz do projeto execute:
```bash
./mvnw clean verify sonar:sonar
```

Na primeira execução, o projeto `ofisy` será criado automaticamente no SonarQube local.

#### 6. Consultar resultado da análise

- Acesse: http://localhost:9000/
- Verifique a última análise executada
  - Para mais detalhes, pode clicar no projeto `ofisy` que será exibido uma visão geral das últimas análises executadas

## Testes de Segurança (OWASP ZAP)

### Executando os testes (Completo)

#### 1. Subir aplicação junto com o ZAP
```bash
docker compose -f compose.yaml -f compose.zap.yaml up --build
```

O processo irá:

1. Subir a aplicação
2. Aguardar o serviço ficar disponível
3. Executar o scan automatizado de segurança

### Relatórios Gerados

Os relatórios são salvos no diretório `./zap-reports/`:

- `full-report.html` - Relatório visual completo
- `full-report.json` - Útil para integração com CI/CD
- `full-report.xml` - Útil para integração com CI/CD e ferramentas externas

### Como visualizar o relatório

Abra o arquivo no navegador: `./zap-reports/full-report.html`

### Limitações atuais

- O scan não autentica automaticamente
- Apenas endpoints públicos são testados
- Endpoints protegidos (JWT/autenticação) não são analisados

### Próximos passos

- Adicionar autenticação (JWT) no scan para expandir a cobertura
- Importar especificação OpenAPI (/v3/api-docs)
- Integrar com pipeline CI/CD