# F06-T04 — ComentarioController

**Fase:** Fase 6 — API (Controller)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F05-T04, F03-T03
**Bloqueia:** F07-T01, F08-T01
**Referência na spec v1.2:** Seção 15 — Controller; Seção 17 — API REST

## Objetivo

Expor os endpoints protegidos de comentário e avaliação, todos aninhados sob o treino ao qual pertencem (consequência de `Comentario` ser embutido, não uma coleção própria).

## Descrição

- Implementar `POST /treinos/{nome}/comentarios`, `GET /treinos/{nome}/comentarios`.
- Implementar `POST /treinos/{nome}/comentarios/{numero}/like` e `POST /treinos/{nome}/comentarios/{numero}/dislike` — sem corpo de requisição. Note que o like/dislike **não** é mais um recurso top-level (`/comentarios/{id}/...` como na v1.1) — é sempre endereçado via o treino (`nome`) + o número do comentário dentro dele.

## Entregáveis

- Classe `ComentarioController`.

## Critérios de aceite

- [ ] Todos os quatro endpoints exigem token válido.
- [ ] `GET /treinos/{nome}/comentarios` retorna a lista já ordenada por score decrescente.
- [ ] `like`/`dislike` para um `numero` que não existe naquele treino retorna 404 (ver F07-T01).
