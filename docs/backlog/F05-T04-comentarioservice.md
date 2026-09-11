# F05-T04 — ComentarioService

**Fase:** Fase 5 — Regras de Negócio (Service)
**Complexidade estimada:** G (P=Pequena, M=Média, G=Grande)
**Depende de:** F04-T04, F02-T02, F03-T03
**Bloqueia:** F06-T04, F09-T02
**Referência na spec v1.2:** Seção 6 — Comentário; Seção 7 — Sistema de Avaliação; Seção 14 — ComentarioService

## Objetivo

Implementar criação de comentário, registro de avaliação (like/dislike) delegando ao Model, e consulta de comentários ordenados por score decrescente — tudo operando sobre a lista `comentarios` embutida no documento `Treino`, sem `ComentarioRepository` (retirado na v1.2 — ver F02-T03).

## Descrição

- **Criar comentário:** carregar o `Treino` por `nome` (`TreinoRepository.findByNome`); calcular `numero` como `(maior numero já existente em treino.getComentarios(), ou 0) + 1`; construir o `Comentario` com `autorNome`/`autorEmail` do usuário autenticado; adicionar à lista `comentarios`; salvar o `Treino` (`TreinoRepository.save`).
- **Registrar avaliação (like/dislike):** carregar o `Treino` por `nome`; localizar, na lista `comentarios`, o item cujo `numero` bate com o parâmetro recebido; chamar `comentario.registrarAvaliacao(email, tipo)`; salvar o `Treino` de volta. Se nenhum comentário com aquele `numero` existir naquele treino, lançar exceção de não encontrado (ver F07-T01).
- **Consultar comentários de um treino:** carregar o `Treino` por `nome`; retornar `treino.getComentarios()` ordenado por `calcularScore()` decrescente — ordenação em memória (`Comparator`), não uma query de repository separada. A ordenação não altera o `numero` de nenhum comentário.

## Entregáveis

- Classe `ComentarioService`.
- Testes unitários: `numero` atribuído sequencialmente e corretamente a cada novo comentário do mesmo treino; like em comentário sem avaliação prévia; troca de like para dislike do mesmo usuário e vice-versa (confirma substituição, não acúmulo); ordenação por score decrescente com empates, confirmando que `numero` permanece estável independente da posição.

## Critérios de aceite

- [ ] Um usuário que já deu like e depois dá dislike no mesmo comentário passa a contar como 1 dislike e 0 likes desse usuário — nunca os dois ao mesmo tempo.
- [ ] A lista de comentários de um treino vem sempre ordenada do maior para o menor score.
- [ ] Dois comentários do mesmo treino nunca recebem o mesmo `numero`; comentários de treinos diferentes podem ter o mesmo `numero` sem conflito (escopo é por treino).
- [ ] Nenhum método público do Service recebe ou retorna um id técnico do MongoDB — só `nome` (do treino) e `numero`/`email` (do comentário/avaliação).
