# Backlog de Implementação — Plataforma de Compartilhamento de Treinos (ODS 3)

Backlog gerado a partir do `spec-plataforma-treinos-v1.2.md`, quebrado em tarefas no estilo **waterfall**: cada tarefa lista suas dependências diretas, e a ordem das fases abaixo é sequencial — uma fase só deve começar quando as dependências das suas tarefas estiverem concluídas (não é necessário esperar a fase anterior inteira terminar, exceto quando explicitamente dependente).

**Nota de versão (v1.2):** este backlog foi revisado por completo na migração de PostgreSQL (relacional) para MongoDB (NoSQL) — ver `decisoes-migracao-nosql.md` para o histórico da decisão. A task **F02-T03 (ComentarioRepository) foi retirada** — `Comentario` deixou de ser coleção própria e passou a ser uma estrutura embutida em `Treino`; o número da task foi mantido como registro histórico (arquivo `F02-T03-comentariorepository.md` agora só contém a nota de retirada), para não precisar renumerar as demais tasks.

## Como usar

- Cada arquivo `F00-T01-...md`, `F01-T01-...md` etc. é uma tarefa independente, com objetivo, descrição, entregáveis e critérios de aceite próprios.
- O campo **Depende de** de cada tarefa é a fonte da verdade sobre a ordem real de execução — não apenas o número da fase.
- Use os critérios de aceite como checklist de Definition of Done de cada tarefa.

## Fases e tarefas


### Fase 0 — Preparação do Ambiente

- **F00-T01** — [Inicialização do projeto Spring Boot](./F00-T01-inicializacao-do-projeto-spring-boot.md) — depende de: Nenhuma (ponto de partida)
- **F00-T02** — [Configuração de banco de dados e ambiente local](./F00-T02-configuracao-de-banco-de-dados-e-ambiente-local.md) — depende de: F00-T01
- **F00-T03** — [Estrutura de pacotes do projeto](./F00-T03-estrutura-de-pacotes-do-projeto.md) — depende de: F00-T01

### Fase 1 — Modelagem de Domínio (Model)

- **F01-T01** — [Entidade Usuario](./F01-T01-entidade-usuario.md) — depende de: F00-T03
- **F01-T02** — [Entidade Treino](./F01-T02-entidade-treino.md) — depende de: F00-T03
- **F01-T03** — [Enum TipoAvaliacao](./F01-T03-enum-tipoavaliacao.md) — depende de: F00-T03
- **F01-T04** — [Estrutura Comentario embutida, com mapa de avaliações](./F01-T04-entidade-comentario-com-mapa-de-avaliacoes.md) — depende de: F01-T02, F01-T03

### Fase 2 — Persistência (Repository)

- **F02-T01** — [UsuarioRepository](./F02-T01-usuariorepository.md) — depende de: F01-T01, F00-T02
- **F02-T02** — [TreinoRepository](./F02-T02-treinorepository.md) — depende de: F01-T02, F00-T02
- ~~F02-T03 — ComentarioRepository~~ — **retirada na v1.2** (ver [nota](./F02-T03-comentariorepository.md)); comentário é persistido via `TreinoRepository`.

### Fase 3 — Segurança (JWT / Spring Security)

- **F03-T01** — [Configuração base do Spring Security](./F03-T01-configuracao-base-do-spring-security.md) — depende de: F00-T01
- **F03-T02** — [Geração e validação de token JWT (JwtUtil)](./F03-T02-geracao-e-validacao-de-token-jwt-jwtutil.md) — depende de: F00-T01
- **F03-T03** — [Filtro de autenticação JWT (JwtFilter)](./F03-T03-filtro-de-autenticacao-jwt-jwtfilter.md) — depende de: F03-T01, F03-T02, F02-T01

### Fase 4 — Contratos da API (DTOs)

- **F04-T01** — [DTOs de Usuario](./F04-T01-dtos-de-usuario.md) — depende de: F01-T01
- **F04-T02** — [DTOs de Auth (login)](./F04-T02-dtos-de-auth-login.md) — depende de: F01-T01
- **F04-T03** — [DTOs de Treino](./F04-T03-dtos-de-treino.md) — depende de: F01-T02
- **F04-T04** — [DTOs de Comentario](./F04-T04-dtos-de-comentario.md) — depende de: F01-T04

### Fase 5 — Regras de Negócio (Service)

- **F05-T01** — [UsuarioService](./F05-T01-usuarioservice.md) — depende de: F02-T01, F04-T01, F03-T01
- **F05-T02** — [AuthService](./F05-T02-authservice.md) — depende de: F02-T01, F04-T02, F03-T02
- **F05-T03** — [TreinoService](./F05-T03-treinoservice.md) — depende de: F02-T02, F04-T03
- **F05-T04** — [ComentarioService](./F05-T04-comentarioservice.md) — depende de: F04-T04, F02-T02, F03-T03 *(v1.2: não depende mais de F02-T03, retirada)*

### Fase 6 — API (Controller)

- **F06-T01** — [UsuarioController](./F06-T01-usuariocontroller.md) — depende de: F05-T01
- **F06-T02** — [AuthController](./F06-T02-authcontroller.md) — depende de: F05-T02
- **F06-T03** — [TreinoController](./F06-T03-treinocontroller.md) — depende de: F05-T03, F03-T03
- **F06-T04** — [ComentarioController](./F06-T04-comentariocontroller.md) — depende de: F05-T04, F03-T03

### Fase 7 — Tratamento de Exceções

- **F07-T01** — [Exceções de negócio e tratamento caso a caso](./F07-T01-excecoes-de-negocio-e-tratamento-caso-a-caso.md) — depende de: F06-T01, F06-T02, F06-T03, F06-T04

### Fase 8 — Documentação (Swagger/OpenAPI)

- **F08-T01** — [Configuração do Swagger/OpenAPI](./F08-T01-configuracao-do-swagger-openapi.md) — depende de: F06-T01, F06-T02, F06-T03, F06-T04, F03-T01

### Fase 9 — Testes

- **F09-T01** — [Testes externos da API (Controllers)](./F09-T01-testes-externos-da-api-controllers.md) — depende de: F08-T01, F07-T01
- **F09-T02** — [Revisão de cobertura de testes unitários (Model + Service)](./F09-T02-revisao-de-cobertura-de-testes-unitarios-model-service.md) — depende de: F01-T04, F05-T01, F05-T02, F05-T03, F05-T04

### Fase 10 — Homologação e Aceite

- **F10-T01** — [Validação final dos critérios de aceitação](./F10-T01-validacao-final-dos-criterios-de-aceitacao.md) — depende de: F09-T01, F09-T02

## Ordem de execução recomendada (visão de alto nível)

```text
F00 Preparação do Ambiente
  └─▶ F01 Modelagem de Domínio
        └─▶ F02 Persistência (Usuario, Treino — sem Comentario) ──┐
        └─▶ F03 Segurança ─────────────────────────────────────────┤
        └─▶ F04 Contratos (DTOs) ──────────────────────────────────┤
                                                                    ▼
                                              F05 Regras de Negócio (Service)
                                                                    │
                                                                    ▼
                                              F06 API (Controller)
                                                                    │
                                                                    ▼
                                              F07 Tratamento de Exceções
                                                                    │
                                                                    ▼
                                              F08 Documentação (Swagger)
                                                                    │
                                                                    ▼
                                              F09 Testes
                                                                    │
                                                                    ▼
                                              F10 Homologação e Aceite
```

Nota: F02 (Persistência), F03 (Segurança) e F04 (DTOs) podem ser desenvolvidas em paralelo entre si — todas dependem apenas de F01, não umas das outras. F05 em diante é sequencial e depende de todas as três.
