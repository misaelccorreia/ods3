# F04-T03 — DTOs de Treino

**Fase:** Fase 4 — Contratos da API (DTOs)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T02
**Bloqueia:** F05-T03
**Referência na spec v1.2:** Seção 16 — DTOs

## Objetivo

Definir o contrato de cadastro e consulta de treino, sem expor autor no corpo da requisição (vem do token) e sem expor id técnico na resposta.

## Descrição

- Criar `TreinoRequestDTO` (`nome`, `descricao`) — sem campo de autor.
- Criar `TreinoResponseDTO` (`nome`, `descricao`, `autorNome`, `autorEmail`, `dataCriacao`) — **sem** `id`. `Treino` já se identifica por `nome`, que o próprio DTO carrega; não há razão para expor o `_id` do MongoDB.

## Entregáveis

- Classes `TreinoRequestDTO` e `TreinoResponseDTO`.

## Critérios de aceite

- [ ] `TreinoRequestDTO` não possui campo de usuário — o autor do treino é sempre resolvido a partir do usuário autenticado no Service/Controller.
- [ ] `TreinoResponseDTO` não possui campo `id`.
