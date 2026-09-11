# F06-T02 — AuthController

**Fase:** Fase 6 — API (Controller)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F05-T02
**Bloqueia:** F07-T01, F08-T01
**Referência na spec v1.2:** Seção 15 — Controller; Seção 17 — API REST

## Objetivo

Expor o endpoint público de login.

## Descrição

- Implementar `POST /login`, recebendo `LoginRequestDTO` e retornando `LoginResponseDTO`.

## Entregáveis

- Classe `AuthController`.

## Critérios de aceite

- [ ] `POST /login` com credenciais corretas retorna um token utilizável nos demais endpoints.
