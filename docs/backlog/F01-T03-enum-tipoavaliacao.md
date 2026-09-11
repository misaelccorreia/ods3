# F01-T03 — Enum TipoAvaliacao

**Fase:** Fase 1 — Modelagem de Domínio (Model)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T03
**Bloqueia:** F01-T04
**Referência na spec v1.2:** Seção 7.2 — Modelagem

## Objetivo

Criar o enum que representa os dois únicos tipos de avaliação possíveis.

## Descrição

- Criar `public enum TipoAvaliacao { LIKE, DISLIKE }` no pacote `model`.
- Sem anotação de persistência própria — quando usado como valor do `Map<String, TipoAvaliacao>` em `Comentario` (F01-T04), o Spring Data MongoDB serializa o enum como string automaticamente.

## Entregáveis

- Enum `TipoAvaliacao`.

## Critérios de aceite

- [ ] O enum possui exatamente dois valores: `LIKE` e `DISLIKE` — nenhum outro tipo de avaliação existe nesta versão.
