# F06-T01 — UsuarioController

**Fase:** Fase 6 — API (Controller)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F05-T01
**Bloqueia:** F07-T01, F08-T01
**Referência na spec v1.2:** Seção 15 — Controller; Seção 17 — API REST

## Objetivo

Expor o endpoint público de cadastro de usuário.

## Descrição

- Implementar `POST /usuarios`, recebendo `UsuarioRequestDTO` e retornando `UsuarioResponseDTO`.

## Entregáveis

- Classe `UsuarioController`.

## Critérios de aceite

- [ ] `POST /usuarios` funciona sem token e retorna o usuário criado sem o campo de senha.
