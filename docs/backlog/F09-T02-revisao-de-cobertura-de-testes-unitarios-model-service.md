# F09-T02 — Revisão de cobertura de testes unitários (Model + Service)

**Fase:** Fase 9 — Testes
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F01-T04, F05-T01, F05-T02, F05-T03, F05-T04
**Bloqueia:** F10-T01
**Referência na spec v1.2:** Seção 20.1 e 20.2 — Testes unitários

## Objetivo

Conferir, ao final da implementação, que todos os casos de teste unitário exigidos pela Seção 20 realmente existem e passam — muitos já devem ter sido escritos junto com F01-T04 e a Fase 5, esta tarefa é a checagem de completude.

## Descrição

- Conferir cobertura de `Comentario.registrarAvaliacao`/`calcularScore` (F01-T04), incluindo a chave por `email`.
- Conferir cobertura de `TreinoService`, `ComentarioService` (incluindo atribuição de `numero` sequencial), `UsuarioService`, `AuthService` (Fase 5) para todos os casos listados na Seção 20.2.

## Entregáveis

- Relatório/checklist de cobertura (pode ser o próprio relatório do JaCoCo ou uma conferência manual dos casos da Seção 20.2).

## Critérios de aceite

- [ ] Nenhum caso listado na Seção 20.1/20.2 da spec está sem teste correspondente.
