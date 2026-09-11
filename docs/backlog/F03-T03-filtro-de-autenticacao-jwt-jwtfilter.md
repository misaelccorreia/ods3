# F03-T03 — Filtro de autenticação JWT (JwtFilter)

**Fase:** Fase 3 — Segurança (JWT / Spring Security)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F03-T01, F03-T02, F02-T01
**Bloqueia:** F05-T04, F06-T03, F06-T04
**Referência na spec v1.2:** Seção 8.1 — Mecanismo; Seção 9 — Arquitetura

## Objetivo

Interceptar toda requisição, extrair e validar o token do header Authorization, e disponibilizar o **email** do usuário autenticado para os Controllers/Services — é essa a peça que faz o email nunca precisar vir no corpo da requisição.

## Descrição

- Criar `JwtFilter extends OncePerRequestFilter`.
- Extrair o token do header `Authorization: Bearer <token>`.
- Validar o token via `JwtUtil`, extrair o `email` (`JwtUtil.extrairEmail`), opcionalmente confirmar que o `Usuario` correspondente existe via `UsuarioRepository.findByEmail`, e popular o `SecurityContext` com essa autenticação (tendo o `email` como principal — não o `id`).
- Registrar o filtro na `SecurityFilterChain` (F03-T01).

## Entregáveis

- Classe `JwtFilter`, integrada à `SecurityConfig`.

## Critérios de aceite

- [ ] Uma requisição autenticada permite que o Controller recupere o email do usuário logado (ex.: via `SecurityContextHolder` ou injeção de `Authentication`) sem receber esse dado no payload.
- [ ] Token inválido ou ausente em rota protegida resulta em requisição rejeitada antes de chegar ao Controller.
