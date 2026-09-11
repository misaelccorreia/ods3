# F00-T03 — Estrutura de pacotes do projeto

**Fase:** Fase 0 — Preparação do Ambiente
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T01
**Bloqueia:** F01-T01, F01-T02, F01-T03
**Referência na spec v1.2:** Seção 11 — Estrutura do Projeto

## Objetivo

Criar os pacotes vazios que organizam o código por responsabilidade, exatamente como definido na spec, para que as tarefas de implementação só precisem adicionar classes nos lugares certos.

## Descrição

- Criar pacotes: `controller`, `service`, `repository`, `model`, `security`, `dto` (com subpacotes `usuario`, `auth`, `treino`, `comentario`), `exception`.
- Não criar pacote/`Repository`/`Service` dedicado a avaliação — essa responsabilidade fica em `ComentarioService` e no Model `Comentario`.
- Não criar `ComentarioRepository` — `Comentario` é uma estrutura embutida em `Treino` (Seção 6), não uma coleção própria; toda persistência de comentário passa por `TreinoRepository`.

## Entregáveis

- Estrutura de diretórios/pacotes criada, mesmo que vazia (pode usar um arquivo `package-info.java` ou classe placeholder para versionar pacotes vazios no Git).

## Critérios de aceite

- [ ] A estrutura de pacotes bate exatamente com a Seção 11 da spec v1.2.
