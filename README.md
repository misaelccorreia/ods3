# ODS3 — Plataforma de Compartilhamento de Treinos

API REST para compartilhamento de treinos e experiências, desenvolvida no contexto da
**ODS 3 — Saúde e Bem-Estar**. Usuários autenticados cadastram treinos, comentam suas
experiências e avaliam os comentários de outros com like/dislike; os comentários são
exibidos por ordem de relevância (`likes - dislikes`).

Projeto **backend apenas**, de escopo didático.

## Stack

Java 21 · Spring Boot 4.1.1 (Web MVC, Security, Validation) · Spring Data MongoDB ·
JJWT 0.12.6 · springdoc-openapi 3.1.0 · JUnit 5 + Mockito

## Estrutura do repositório

```text
ods3/                 aplicação Spring Boot
docs/
  spec-plataforma-treinos-v1.2.md    especificação — fonte da verdade
  backlog/                            tarefas de implementação (00-INDEX.md)
  mongodb-docker-setup.md             setup do MongoDB local via Docker
  cobertura-testes.md                 conferência da Seção 20 da spec contra os testes
  validacao-final.md                  critérios de aceitação da Seção 21, com evidências
docker-compose.yml    MongoDB local (mongo:7.0)
```

## Rodando localmente

Requer Java 21, Maven e Docker.

```bash
docker compose up -d
cd ods3 && mvn spring-boot:run
```

Se já existir um container chamado `mongodb` criado fora do compose, use `docker start mongodb`.

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html` — cadastre um usuário em `POST /usuarios`,
  faça login em `POST /login`, copie o token e cole em **Authorize** para chamar os demais endpoints.

O segredo de assinatura do JWT tem um valor padrão só para desenvolvimento. Fora do ambiente
local, defina a variável `JWT_SECRET` (base64, 256 bits ou mais).

## Endpoints

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/usuarios` | público |
| POST | `/login` | público |
| POST | `/treinos` | token |
| GET | `/treinos` | token |
| GET | `/treinos/{nome}` | token |
| POST | `/treinos/{nome}/comentarios` | token |
| GET | `/treinos/{nome}/comentarios` | token |
| POST | `/treinos/{nome}/comentarios/{numero}/like` | token |
| POST | `/treinos/{nome}/comentarios/{numero}/dislike` | token |

Treinos são endereçados pelo `nome` e comentários pelo `numero` dentro do treino — nenhum endpoint
recebe ou devolve o id do MongoDB.

## Testes

```bash
cd ods3 && mvn test
```

77 testes: unitários de Model, Service e JWT, mais 27 testes externos (`ApiExternaTest`) que fazem
requisições HTTP reais contra a aplicação. Os externos precisam do MongoDB no ar e usam um banco
próprio (`ods3_teste_api`), apagado ao final.
