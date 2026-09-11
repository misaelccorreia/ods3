# F01-T02 — Entidade Treino

**Fase:** Fase 1 — Modelagem de Domínio (Model)
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T03
**Bloqueia:** F01-T04, F02-T02, F04-T03
**Referência na spec v1.2:** Seção 5 — Treino; Seção 18 — MongoDB

## Objetivo

Modelar a entidade Treino como agregado MongoDB, incluindo a lista embutida de comentários e o recorte denormalizado do autor.

## Descrição

- Criar classe `Treino` anotada com `@Document(collection = "treinos")`.
- Campo `id` do tipo `String`, anotado `@Id` — gerado pelo MongoDB, nunca preenchido pela aplicação.
- Anotar `nome` com `@Indexed(unique = true)`.
- Campos `descricao`, `autorNome`, `autorEmail` — **não** criar relação `@DBRef`/referência para `Usuario` inteiro; são apenas strings copiadas do usuário autenticado no momento da criação (Service, F05-T03).
- Campo `dataCriacao`, preenchido automaticamente na criação e nunca alterado depois — é só auditoria, não participa de nenhuma regra de negócio.
- Campo `comentarios` do tipo `List<Comentario>` (inicializado vazio) — lista embutida, serializada pelo Spring Data MongoDB como array de sub-documentos dentro do próprio `Treino`. Depende de `Comentario` existir (F01-T04) para compilar, mas por ser um campo simples, pode ser deixado como `List<Object>`/adicionado depois se a ordem de implementação exigir — o importante é que o campo exista no documento final.

## Entregáveis

- Classe `Treino` anotada com Spring Data MongoDB, incluindo o campo `comentarios` embutido.

## Critérios de aceite

- [ ] A entidade persiste corretamente, incluindo `autorNome`/`autorEmail` e o preenchimento automático de `dataCriacao`.
- [ ] Tentar inserir dois treinos com o mesmo `nome` falha por violação do índice único.
- [ ] `comentarios` é serializado como array embutido dentro do documento `Treino` (confirmável inspecionando o documento no MongoDB), não como coleção separada.
