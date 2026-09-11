# F09-T01 — Testes externos da API (Controllers)

**Fase:** Fase 9 — Testes
**Complexidade estimada:** G (P=Pequena, M=Média, G=Grande)
**Depende de:** F08-T01, F07-T01
**Bloqueia:** F10-T01
**Referência na spec v1.2:** Seção 20.3 — Controller (testes externos)

## Objetivo

Validar, por requisições HTTP reais, o comportamento de todos os endpoints — já que Controllers não recebem teste unitário nesta spec.

## Descrição

- Cobrir, para cada endpoint: caso de sucesso, caso de validação (payload inválido) e caso de erro de negócio.
- Cobrir explicitamente: cadastro de usuário, login (sucesso/falha), cadastro de treino (novo e duplicado — conferir 201 vs 200), consulta de treino por `nome` (existente e inexistente), cadastro de comentário (conferir `numero` sequencial), like/dislike por `nome`+`numero` (incluindo troca de voto e `numero` inexistente), consulta ordenada por score, e rejeição de chamada sem token em rota protegida.

## Entregáveis

- Suíte de testes externos (ex.: `@SpringBootTest` com `TestRestTemplate`/`MockMvc`, ou coleção Postman/Insomnia com script de execução — à escolha do desenvolvedor).

## Critérios de aceite

- [ ] Todos os critérios de aceitação da Seção 21 relacionados a comportamento de endpoint são exercitados por pelo menos um teste externo.
