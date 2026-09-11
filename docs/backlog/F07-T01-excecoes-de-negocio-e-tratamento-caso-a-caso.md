# F07-T01 — Exceções de negócio e tratamento caso a caso

**Fase:** Fase 7 — Tratamento de Exceções
**Complexidade estimada:** M (P=Pequena, M=Média, G=Grande)
**Depende de:** F06-T01, F06-T02, F06-T03, F06-T04
**Bloqueia:** F09-T01
**Referência na spec v1.2:** Seção 11 — pacote exception; Changelog v1.1 → v1.2

## Objetivo

Criar as exceções de negócio necessárias e garantir que cada uma seja tratada com um código HTTP adequado, sem adotar um contrato de erro global padronizado (decisão explícita da spec).

## Descrição

- Criar exceções conforme a necessidade identificada nas Fases 5/6, por exemplo: e-mail já cadastrado, credenciais inválidas, **treino não encontrado por `nome`**, **comentário não encontrado por `numero` dentro de um treino**.
- Tratar cada exceção no Controller correspondente (ou via `@ExceptionHandler` pontual na própria classe) com o status HTTP adequado ao caso — não é necessário (nem esperado) um `@ControllerAdvice` global único para todo o projeto.

## Entregáveis

- Classes de exceção no pacote `exception/`.
- Tratamento correspondente em cada Controller/Service afetado.

## Critérios de aceite

- [ ] Cada cenário de erro de negócio identificado nas Fases 5 e 6 retorna um status HTTP e uma mensagem coerentes, validados manualmente ou pelos testes externos (Fase 9).
- [ ] Buscar treino por `nome` inexistente e dar like/dislike em `numero` inexistente retornam 404 com mensagens que identificam qual dos dois não foi encontrado.
