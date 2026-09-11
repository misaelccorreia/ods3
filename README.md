# ODS3 — Plataforma de Compartilhamento de Treinos

API REST para compartilhamento de treinos e experiências, desenvolvida no contexto da
**ODS 3 — Saúde e Bem-Estar**. Usuários autenticados cadastram treinos, comentam suas
experiências e avaliam os comentários de outros com like/dislike; os comentários são
exibidos por ordem de relevância (`likes - dislikes`).

Projeto **backend apenas**, de escopo didático.

## Stack

Java 21 · Spring Boot 4.1.1 (Web MVC, Security, Validation) · Spring Data MongoDB ·
JJWT 0.12.6 · springdoc-openapi 3.1.0 · JUnit + Mockito

## Estrutura do repositório

```text
ods3/                 aplicação Spring Boot
docs/                 spec, backlog e decisões de arquitetura
  spec-plataforma-treinos-v1.2.md    especificação — fonte da verdade
  backlog/                            tarefas de implementação (00-INDEX.md)
  mongodb-docker-setup.md             setup do MongoDB local via Docker
docker-compose.yml    MongoDB local (mongo:7.0)
```

## Rodando localmente

Requer Java 21, Maven e Docker.

```bash
docker compose up -d
cd ods3 && mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080` e conecta no MongoDB em
`mongodb://localhost:27017/plataforma_treinos`. Documentação da API (quando os
endpoints existirem) em `/swagger-ui.html`.

## Status

Fase 0 (preparação do ambiente) concluída — build validado e conexão com o MongoDB
confirmada. Implementação segue o backlog em `docs/backlog/00-INDEX.md`.
