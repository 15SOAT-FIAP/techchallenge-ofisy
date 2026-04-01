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