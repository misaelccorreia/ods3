# F02-T03 — [RETIRADA na v1.2] ComentarioRepository

**Status:** Retirada do backlog na revisão v1.2 (migração PostgreSQL → MongoDB).

## Por que essa task não existe mais

Na v1.1, `Comentario` era uma entidade JPA própria, com sua própria tabela e repository (`ComentarioRepository extends JpaRepository<Comentario, Long>`, com `findByTreinoId`).

Na v1.2, `Comentario` deixou de ser uma coleção própria — passou a ser uma estrutura **embutida** dentro do documento `Treino` (Seção 6 da spec v1.2). Não existe mais nada para um `ComentarioRepository` fazer: toda leitura/escrita de comentário acontece através do `TreinoRepository` (F02-T02), carregando e salvando o `Treino` inteiro.

## O que ficou no lugar

- Persistência de comentário: `TreinoRepository` (F02-T02).
- Lógica de criar comentário / registrar avaliação / listar ordenado por score: `ComentarioService` (F05-T04), operando sobre a lista embutida `treino.getComentarios()`.

Este arquivo é mantido apenas como registro histórico do número de task (evita reindexar as demais tasks do backlog). Nenhuma outra task depende deste arquivo.
