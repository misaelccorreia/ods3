# F02-T01 — UsuarioRepository

**Fase:** Fase 2 — Persistência (Repository)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T01, F00-T02
**Bloqueia:** F03-T03, F05-T01, F05-T02
**Referência na spec v1.2:** Seção 13 — Repository; Seção 14 — UsuarioService

## Objetivo

Prover acesso a persistência para Usuario, incluindo a busca por e-mail usada no login e na resolução do usuário autenticado a partir do token.

## Descrição

- Criar `UsuarioRepository extends MongoRepository<Usuario, String>`.
- Adicionar `Optional<Usuario> findByEmail(String email)`.
- Adicionar `boolean existsByEmail(String email)` (usado pelo `UsuarioService` para validar unicidade antes de cadastrar).
- Não é necessário nenhum método baseado em `id` — a aplicação nunca localiza um `Usuario` pelo `_id` do Mongo.

## Entregáveis

- Interface `UsuarioRepository`.

## Critérios de aceite

- [ ] `findByEmail` e `existsByEmail` retornam resultados corretos em um teste manual/rápido contra o banco de desenvolvimento.
