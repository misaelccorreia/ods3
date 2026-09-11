# F00-T01 — Inicialização do projeto Spring Boot

**Fase:** Fase 0 — Preparação do Ambiente
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** Nenhuma (ponto de partida)
**Bloqueia:** F00-T02, F00-T03, F03-T01, F03-T02
**Referência na spec v1.2:** Seção 10 — Tecnologias; Seção 11 — Estrutura do Projeto

## Objetivo

Criar o esqueleto do projeto Spring Boot com todas as dependências que o restante do backlog vai exigir, para que nenhuma tarefa posterior precise voltar aqui para adicionar biblioteca.

## Descrição

- Gerar o projeto (Spring Initializr ou equivalente) com Java + Maven/Gradle.
- Adicionar dependências: Spring Web, **Spring Data MongoDB**, Spring Security, Validation.
- Adicionar biblioteca de JWT (ex.: io.jsonwebtoken:jjwt-api/impl/jackson, ou equivalente).
- Adicionar springdoc-openapi (Swagger) para Spring Boot.
- Definir o groupId/artifactId consistente com o pacote `com.projeto.ods3` usado na spec.
- **Não** adicionar Spring Data JPA nem driver PostgreSQL — não fazem parte da v1.2.

## Entregáveis

- Projeto compilável (`mvn clean install` ou `gradle build` sem erros) com dependências vazias de código.
- `pom.xml`/`build.gradle` com todas as dependências listadas.

## Critérios de aceite

- [ ] O projeto builda do zero sem erros de dependência.
- [ ] A aplicação sobe (`run`) e expõe a porta padrão do Spring Boot, mesmo sem endpoints implementados.

## Observações

Esta tarefa é o único ponto de partida do backlog — todas as demais dependem, direta ou indiretamente, dela.
