# Validação final — critérios de aceitação da Seção 21 (F10-T01)

Cada item da Seção 21 da spec v1.2 conferido contra a aplicação, com a evidência correspondente.

**Execução de referência:** `mvn test` em 2026-09-11 — **77 testes, 0 falhas**. Os 27 testes de
`ApiExternaTest` sobem a aplicação real numa porta aleatória e fazem requisições HTTP de verdade
contra ela, com MongoDB real (banco `ods3_teste_api`), sem mocks. O item 17 foi conferido também
pela Swagger UI com a aplicação rodando em `localhost:8080`.

| # | Critério (Seção 21) | Evidência | Situação |
|---:|---|---|:---:|
| 1 | Um usuário pode ser cadastrado, com senha em hash | `ApiExternaTest › cadastroDeUsuarioDevolve201SemSenhaNemId`; `senhaFicaGravadaComoHashNoBanco` lê o documento cru no MongoDB e confere hash BCrypt (`$2…`) diferente da senha enviada; `UsuarioServiceTest › cadastraComSenhaEmHash` | ✅ |
| 2 | Um usuário pode autenticar e obter um token JWT com claim `sub` = e-mail | `loginValidoDevolveTokenQueAbreRotasProtegidas`; `tokenTemSubIgualAoEmail` decodifica o payload do token devolvido pela API; `JwtUtilTest › claimSubEhOEmail` | ✅ |
| 3 | Endpoints protegidos rejeitam requisições sem token válido | `rotasProtegidasSemTokenOuComTokenInvalidoDevolvem401` — as 7 rotas protegidas, sem token e com token inválido, todas 401; `JwtUtilTest` cobre token adulterado, expirado e de outra chave | ✅ |
| 4 | Um usuário autenticado pode cadastrar um treino | `cadastroDeTreinoNovoDevolve201ComAutorDoTokenELocation` — 201, autor vindo do token, `Location` aponta para o treino | ✅ |
| 5 | A API verifica a existência do treino (nome, global, case-sensitive) antes de criá-lo | `cadastroDeTreinoRepetidoDevolve200ComOExistenteSemDuplicar` (outro usuário tentando o mesmo nome — escopo global); `nomeComCaixaDiferenteCriaOutroTreino` (case-sensitive); `TreinoServiceTest` | ✅ |
| 6 | Um treino existente é retornado com HTTP 200 em vez de duplicado | `cadastroDeTreinoRepetidoDevolve200ComOExistenteSemDuplicar` — 200, dados originais preservados, 1 documento no banco | ✅ |
| 7 | Um treino pode ser consultado pelo `nome`, sem id técnico na URL | `consultaDeTreinoPorNome` (nome com espaço e acento: `Glúteo e Posterior`); `idTecnicoNaoFuncionaComoEnderecoDeTreino` — o `_id` real do documento usado na URL dá 404 | ✅ |
| 8 | Um treino pode receber múltiplos comentários, cada um com `numero` sequencial | `comentariosRecebemNumeroSequencialEAutorDoToken` (1, 2, 3); `numeroDeComentarioEhEscopadoPorTreino` | ✅ |
| 9 | Comentários não podem ter o texto alterado ou excluído | `comentarioNaoPodeSerEditadoNemExcluido` — PUT, PATCH e DELETE não existem (404/405) e o texto segue igual; `ComentarioTest › semSetterDeTextoOuNumero` | ✅ |
| 10 | Um usuário pode dar like ou dislike num comentário identificado por `nome` + `numero` | `likeEDislikeComTrocaDeVotoSemAcumular`; `avaliarNumeroInexistenteDevolve404IdentificandoOComentario`; `avaliarEmTreinoInexistenteDevolve404IdentificandoOTreino` | ✅ |
| 11 | Um usuário pode trocar o voto e só o mais recente conta | `likeEDislikeComTrocaDeVotoSemAcumular` — like → dislike → like → like repetido, sempre um voto por usuário; `ComentarioServiceTest › trocaDe*`; `ComentarioTest › substitui*` | ✅ |
| 12 | O score é `likes - dislikes` sobre as avaliações vigentes | `ComentarioTest › calcularScore` (positivo, empate, negativo, sem votos); `likes`, `dislikes` e `score` conferidos nas respostas da API | ✅ |
| 13 | Comentários vêm em ordem decrescente de score, sem alterar o `numero` | `comentariosVemOrdenadosPorScoreENumeroNaoMuda` — ordem `[2, 3, 1]`, votos mudam, reordena para `[3, 1, 2]` e o comentário 2 continua sendo o 2; `ComentarioServiceTest › listaOrdenada…` | ✅ |
| 14 | Nenhum endpoint de listagem aplica paginação | `listagemDeTreinosNaoPagina` — 30 treinos devolvidos numa única resposta; nenhum endpoint aceita parâmetro de página (conferido no documento OpenAPI) | ✅ |
| 15 | Os dados são persistidos no MongoDB, com `Comentario` embutido em `Treino` | `comentariosFicamEmbutidosNoDocumentoDoTreino` — só existem as coleções `usuarios` e `treinos`; os comentários são array dentro do documento, com as avaliações aninhadas | ✅ |
| 16 | Nenhum endpoint aceita ou expõe id técnico em request ou response | toda resposta de sucesso da suíte externa passa por `assertSemCamposTecnicos` (sem `id`, `_id`, `versao`, `_class`, `senha`); item 7; schemas do OpenAPI sem `id` | ✅ |
| 17 | A API está documentada no Swagger com suporte a Bearer JWT | Esquema `bearerAuth` (HTTP bearer, JWT) com segurança global, `/usuarios` e `/login` marcados como públicos; fluxo completo executado pela Swagger UI — ver seção abaixo | ✅ |
| 18 | Models possuem testes unitários, incluindo `registrarAvaliacao`/`calcularScore` | `ComentarioTest` — 13 testes | ✅ |
| 19 | Services possuem testes unitários | `UsuarioServiceTest` (3), `AuthServiceTest` (3), `TreinoServiceTest` (8), `ComentarioServiceTest` (15) | ✅ |
| 20 | Controllers são validados por testes externos | `ApiExternaTest` — 27 testes por HTTP real | ✅ |
| 21 | Repositories usam as abstrações do Spring Data MongoDB | `UsuarioRepository` e `TreinoRepository` estendem `MongoRepository`, só com consultas derivadas (`findByEmail`, `existsByEmail`, `findByNome`, `existsByNome`) | ✅ |

## Item 17 — Swagger

Conferido em 2026-09-11 pela própria Swagger UI (`/swagger-ui.html`), com a aplicação rodando em `localhost:8080`:

1. `POST /usuarios` → *Try it out* → *Execute*: usuário `swagger@exemplo.com` criado — conferido no MongoDB, com a
   senha gravada como hash BCrypt (`$2a$…`, 60 caracteres).
2. `POST /login` → *Execute*: resposta com o token; payload decodificado `{"sub":"swagger@exemplo.com", "iat": …, "exp": …}`.
3. *Authorize* → token colado no campo de `bearerAuth` → *Apply credentials*: os 7 endpoints protegidos passam a
   exibir o cadeado fechado.
4. `GET /treinos` → *Try it out* → *Execute*: **200**, corpo `[]`. O comando curl gerado pela UI mostra que ela enviou
   `Authorization: Bearer <token>`. Sem token, a mesma rota responde 401.

Documento OpenAPI (`/v3/api-docs`) conferido: códigos de resposta declarados por endpoint (ex.: `POST /treinos` com
200/201/400, `GET /treinos/{nome}` com 200/404, like/dislike com 200/404/409), path variables `{nome}` e `{numero}`
descritos, nenhum schema de resposta com `id`, `versao` ou `senha`, e o parâmetro `Authentication` dos controllers
fora da documentação.

## Decisões tomadas durante a implementação

Pontos em que a spec não bastava ou se contradizia, e o que foi decidido. Todos estão registrados
também na própria spec ou no backlog.

- **`UsuarioResponseDTO` sem `id`.** A Seção 16 mantinha o `id` como "única exceção", o que contradizia
  a Seção 4 e o item 16 acima. Prevaleceu a regra geral.
- **Escape do ponto nas chaves de `avaliacoes`.** O MongoDB não aceita ponto em nome de campo e a chave
  é o e-mail; o `MongoConfig` grava `ana@exemplo[dot]com` e desfaz a troca na leitura (Seção 7.2).
- **Campo `versao` em `Treino` (`@Version`).** Comentar e votar regravam o treino inteiro; sem controle
  de versão, duas gravações simultâneas fariam uma apagar a outra. O `ComentarioService` retenta em conflito.
- **Nome de treino sem `/`, `\`, `%` e `;`.** O nome vira segmento de URL, e esses caracteres o tornariam
  inalcançável em `GET /treinos/{nome}` (o firewall do Spring Security rejeita as formas codificadas).
- **Classes de configuração na raiz do pacote.** `MongoConfig` e `OpenApiConfig` ficam em `com.projeto.ods3`
  para não criar um pacote `config` que a Seção 11 não prevê.
