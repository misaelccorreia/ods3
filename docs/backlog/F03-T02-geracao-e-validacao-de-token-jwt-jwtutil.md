# F03-T02 — Geração e validação de token JWT (JwtUtil)

**Fase:** Fase 3 — Segurança (JWT / Spring Security)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T01
**Bloqueia:** F03-T03, F05-T02
**Referência na spec v1.2:** Seção 8.1 — Mecanismo

## Objetivo

Encapsular a geração e validação de tokens JWT em um componente único, usado pelo AuthService (para emitir) e pelo JwtFilter (para validar).

## Descrição

- Criar `JwtUtil` com métodos `gerarToken(String email)`, `validarToken(String token)` e `extrairEmail(String token)`.
- O claim `sub` do token é o **`email`** do usuário — nunca um id técnico do MongoDB (Seção 8.1 da spec v1.2).
- Definir segredo/algoritmo de assinatura e tempo de expiração do token (parametrizável via `application.yml`).

## Entregáveis

- Classe `JwtUtil`.

## Critérios de aceite

- [ ] Um token gerado por `gerarToken` é validado com sucesso por `validarToken`.
- [ ] `extrairEmail` devolve exatamente o email usado em `gerarToken`.
- [ ] Um token adulterado ou expirado falha na validação.
