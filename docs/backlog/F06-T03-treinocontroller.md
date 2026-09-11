# F06-T03 — TreinoController

**Fase:** Fase 6 — API (Controller)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F05-T03, F03-T03
**Bloqueia:** F07-T01, F08-T01
**Referência na spec v1.2:** Seção 15 — Controller; Seção 17 — API REST

## Objetivo

Expor os endpoints protegidos de treino, endereçando a consulta individual por `nome` — nunca por id técnico.

## Descrição

- Implementar `POST /treinos`, `GET /treinos`, `GET /treinos/{nome}`.
- `POST /treinos`: se o treino já existir, retornar HTTP 200 com o existente; se for novo, HTTP 201 (Seção 5).
- `GET /treinos/{nome}`: se não existir treino com aquele nome, retornar 404 (ver F07-T01).

## Entregáveis

- Classe `TreinoController`.

## Critérios de aceite

- [ ] Os três endpoints exigem token válido.
- [ ] Cadastrar um treino repetido retorna 200, não 201 nem 409.
- [ ] `GET /treinos/{nome}` funciona pelo nome do treino, sem nenhum id na URL.
