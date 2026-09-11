# F02-T02 — TreinoRepository

**Fase:** Fase 2 — Persistência (Repository)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T02, F00-T02
**Bloqueia:** F05-T03, F05-T04
**Referência na spec v1.2:** Seção 13 — Repository; Seção 5 — Regra de negócio; Seção 18 — MongoDB

## Objetivo

Prover acesso a persistência para Treino (e, por consequência, para os comentários embutidos nele), incluindo a verificação de existência por nome exigida pela regra de não duplicidade.

## Descrição

- Criar `TreinoRepository extends MongoRepository<Treino, String>`.
- Adicionar `boolean existsByNome(String nome)` — comparação exata (não usar `existsByNomeIgnoreCase` nem `Containing`, para preservar o case-sensitive e a ausência de fuzzy match definidos na Seção 5).
- Adicionar `Optional<Treino> findByNome(String nome)` — usado pelo `TreinoService` para retornar o treino já existente e pelo endpoint `GET /treinos/{nome}`, e usado pelo `ComentarioService` (F05-T04) como ponto de entrada para ler/gravar a lista embutida de comentários.
- Este repository é também o único caminho de persistência para `Comentario` — não existe `ComentarioRepository` (a busca de comentários de um treino é a leitura do campo `comentarios` do `Treino` já carregado, não uma query separada).

## Entregáveis

- Interface `TreinoRepository`.

## Critérios de aceite

- [ ] `existsByNome("Treino de Perna")` e `existsByNome("treino de perna")` são tratados como buscas diferentes (confirma case-sensitive).
- [ ] `findByNome` retorna o `Treino` com sua lista `comentarios` completa (embutida no mesmo documento).
