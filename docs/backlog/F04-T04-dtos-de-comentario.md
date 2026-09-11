# F04-T04 — DTOs de Comentario

**Fase:** Fase 4 — Contratos da API (DTOs)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T04
**Bloqueia:** F05-T04
**Referência na spec v1.2:** Seção 16 — DTOs

## Objetivo

Definir o contrato de criação e consulta de comentário, incluindo a exposição de likes/dislikes/score calculados e do `numero` usado para endereçar like/dislike.

## Descrição

- Criar `ComentarioRequestDTO` (`texto`) — sem autor/treino no corpo (o treino vem da URL, o usuário do token).
- Criar `ComentarioResponseDTO` (`numero`, `texto`, `autorNome`, `autorEmail`, `dataCriacao`, `likes`, `dislikes`, `score`) — **sem** `id`. `numero` é o dado que o cliente usa para montar a URL de like/dislike (`POST /treinos/{nome}/comentarios/{numero}/like`).
- Não criar `AvaliacaoRequestDTO` — os endpoints de like/dislike não recebem corpo.

## Entregáveis

- Classe `ComentarioRequestDTO` e `ComentarioResponseDTO`.

## Critérios de aceite

- [ ] `ComentarioResponseDTO` expõe `likes`, `dislikes` e `score` já calculados a partir do mapa de avaliações do Model.
- [ ] `ComentarioResponseDTO` expõe `numero`, não `id`.
