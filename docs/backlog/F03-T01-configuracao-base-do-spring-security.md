# F03-T01 — Configuração base do Spring Security

**Fase:** Fase 3 — Segurança (JWT / Spring Security)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T01
**Bloqueia:** F03-T03, F05-T01, F08-T01
**Referência na spec v1.2:** Seção 8 — Autenticação e Segurança

## Objetivo

Configurar o Spring Security definindo quais rotas são públicas e quais exigem autenticação, e disponibilizar o PasswordEncoder usado pelo cadastro de usuário.

## Descrição

- Criar `SecurityConfig` com `SecurityFilterChain`.
- Liberar (`permitAll`) apenas `POST /usuarios` e `POST /login`.
- Exigir autenticação para todas as demais rotas, **incluindo os GETs** de treino e comentário — regra definitiva da Seção 8.2 da spec v1.2 (não é mais uma decisão pendente de confirmação).
- Declarar bean `PasswordEncoder` (ex.: `BCryptPasswordEncoder`).
- Desabilitar CSRF (API stateless) e configurar sessão como `STATELESS`.

## Entregáveis

- Classe `SecurityConfig`.

## Critérios de aceite

- [ ] Requisição sem token a uma rota protegida retorna 401/403.
- [ ] Requisição a `POST /usuarios` e `POST /login` funciona sem token.
