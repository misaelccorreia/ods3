# F01-T04 — Estrutura Comentario embutida, com mapa de avaliações

**Fase:** Fase 1 — Modelagem de Domínio (Model)
**Complexidade estimada:** G (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T02, F01-T03
**Bloqueia:** F04-T04, F09-T02
**Referência na spec v1.2:** Seção 6 — Comentário; Seção 6.1 — Imutabilidade; Seção 7 — Sistema de Avaliação

## Objetivo

Modelar `Comentario` como estrutura **embutida** (não `@Document`, não coleção própria) com o mapa de avaliações e o comportamento de domínio (registrar avaliação, calcular score), consistente com `Comentario` vivendo dentro da lista `comentarios` de `Treino` (F01-T02).

## Descrição

- Criar classe `Comentario` **sem** `@Document` e **sem** `@Id` — é um objeto simples (POJO), serializado pelo Spring Data MongoDB como sub-documento embutido dentro de `Treino`.
- Campos: `numero` (int — sequencial dentro do treino, atribuído pelo `ComentarioService` na criação, F05-T04; o Model apenas guarda o valor, não o calcula), `texto`, `autorNome`, `autorEmail`, `dataCriacao`.
- Adicionar `private Map<String, TipoAvaliacao> avaliacoes = new HashMap<>()` — chave é o **`email`** do usuário que votou (não um id técnico).
- **Pré-requisito descoberto na execução:** o MongoDB rejeita ponto em nome de campo, e e-mail sempre tem ponto. É preciso configurar `MappingMongoConverter.setMapKeyDotReplacement("[dot]")` (classe `MongoConfig`), senão qualquer avaliação falha ao gravar. Ver nota na Seção 7.2 da spec.
- Implementar `public void registrarAvaliacao(String email, TipoAvaliacao tipo)` — `avaliacoes.put(email, tipo)` (upsert natural do Map).
- Implementar `public int calcularScore()` — conta `LIKE` menos `DISLIKE` no mapa.
- Não expor setter para `texto` após a criação — reforça a imutabilidade descrita na Seção 6.1 (nenhum método de alteração de texto deve existir na classe).
- Não expor setter para `numero` após a criação — é atribuído uma única vez, na construção do objeto pelo Service.

## Entregáveis

- Classe `Comentario` completa, incluindo os dois métodos de domínio.
- Testes unitários de Model cobrindo: criar avaliação nova, substituir avaliação existente do mesmo `email` (like→dislike e dislike→like), calcular score com empate e com score negativo.

## Critérios de aceite

- [ ] `registrarAvaliacao` chamado duas vezes para o mesmo `email` com tipos diferentes resulta em apenas uma entrada no mapa, com o tipo mais recente.
- [ ] `calcularScore()` retorna `likes - dislikes` corretamente, inclusive quando o resultado é negativo.
- [ ] Não existe nenhum método público que altere `texto` ou `numero` depois de o comentário ser criado.
- [ ] A classe não possui `@Document`/`@Id` — não é persistida como coleção própria.
