# F01-T01 — Entidade Usuario

**Fase:** Fase 1 — Modelagem de Domínio (Model)
**Complexidade estimada:** P (P=Pequena, M=Média, G=Grande)
**Depende de:** F00-T03
**Bloqueia:** F01-T04, F02-T01, F04-T01, F04-T02
**Referência na spec v1.2:** Seção 4.1 — Usuário

## Objetivo

Modelar a entidade Usuario como documento MongoDB.

## Descrição

- Criar classe `Usuario` anotada com `@Document(collection = "usuarios")`.
- Campo `id` do tipo `String`, anotado `@Id` (org.springframework.data.annotation.Id) — gerado pelo MongoDB, **nunca preenchido pela aplicação**. Não escrever nenhum código que atribua valor a esse campo antes de salvar.
- Campos `nome`, `email`, `senha`.
- Anotar `email` com `@Indexed(unique = true)` — substitui a constraint de unicidade que em JPA seria `@Column(unique = true)`.
- A `senha` é armazenada como hash — a entidade apenas guarda a string do hash; a geração do hash é responsabilidade do Service (F05-T01), não do Model.

## Entregáveis

- Classe `Usuario` anotada com Spring Data MongoDB (`@Document`, `@Id`, `@Indexed`).

## Critérios de aceite

- [ ] A entidade persiste e recupera corretamente via `MongoRepository` (teste manual ou via repository já nesta tarefa, mesmo que o Repository formal só seja criado na Fase 2).
- [ ] Tentar inserir dois usuários com o mesmo e-mail falha por violação do índice único.
- [ ] Nenhum código da aplicação atribui valor ao campo `id` — só o MongoDB gera.
