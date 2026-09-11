# Cobertura de testes — conferência da Seção 20 da spec v1.2 (F09-T02)

Conferência manual, caso a caso, dos itens das Seções 20.1 e 20.2 contra os testes que existem no projeto.
Todos os testes rodam com `mvn test` (os testes externos exigem o MongoDB local no ar).

| Suíte | Tipo | Testes |
|---|---|---:|
| `model/ComentarioTest` | unitário de Model | 13 |
| `security/JwtUtilTest` | unitário | 7 |
| `service/UsuarioServiceTest` | unitário de Service (JUnit + Mockito) | 3 |
| `service/AuthServiceTest` | unitário de Service (JUnit + Mockito) | 3 |
| `service/TreinoServiceTest` | unitário de Service (JUnit + Mockito) | 8 |
| `service/ComentarioServiceTest` | unitário de Service (JUnit + Mockito) | 15 |
| `controller/ApiExternaTest` | externo — HTTP real contra a aplicação (Seção 20.3) | 27 |
| `Ods3ApplicationTests` | subida do contexto | 1 |

## 20.1 Model

| Caso da spec | Teste |
|---|---|
| `registrarAvaliacao` cria avaliação nova | `ComentarioTest › registrarAvaliacao › criaAvaliacaoNova` |
| `registrarAvaliacao` substitui avaliação existente do mesmo e-mail | `substituiLikePorDislike`, `substituiDislikePorLike`, `repetirMesmoVotoNaoAcumula` |
| chave do mapa é o e-mail (um voto por usuário) | `usuariosDiferentesCoexistem`; no banco: `ApiExternaTest › comentariosFicamEmbutidosNoDocumentoDoTreino` |
| `calcularScore` = likes − dislikes | `ComentarioTest › calcularScore › scorePositivo` |
| `calcularScore` com empate | `scoreEmpatado` |
| `calcularScore` negativo | `scoreNegativo` |
| `calcularScore` sem avaliações | `semAvaliacoes` |
| imutabilidade de texto e número (Seção 6.1) | `ComentarioTest › imutabilidade › semSetterDeTextoOuNumero`, `mapaDeAvaliacoesNaoEditavelPorFora` |
| `Comentario` embutido, sem `@Document`/`@Id` | `naoEhDocumentoProprio` |
| `TipoAvaliacao` só com LIKE e DISLIKE | `ComentarioTest › TipoAvaliacao › doisValores` |
| Treino / Usuario — regras próprias, se houver | Não há comportamento de domínio nessas entidades além de getters/construtor; não se aplica. |

## 20.2 Service

| Caso da spec | Teste |
|---|---|
| treino existente — retorna o existente, não duplica | `TreinoServiceTest › treinoExistenteEhRetornadoSemDuplicar` |
| treino inexistente — cria novo | `treinoInexistenteEhCriadoComAutorDoToken` |
| criação de comentário — `numero` sequencial começando em 1 | `ComentarioServiceTest › numeroEhSequencialComecandoEmUm`, `numeroEhMaiorExistenteMaisUmNaoTamanhoDaLista`, `numeroEhEscopadoPorTreino` |
| like em comentário sem avaliação prévia | `likeEmComentarioSemAvaliacaoPrevia` |
| troca de like para dislike (e vice-versa) — substitui, não acumula | `trocaDeLikeParaDislikeSubstituiNaoAcumula`, `trocaDeDislikeParaLikeSubstituiNaoAcumula` |
| cálculo do score | `ComentarioTest › calcularScore › *`; score conferido nos DTOs em `trocaDe*` e `listaOrdenada*` |
| ordenação por score decrescente sem afetar o `numero` | `listaOrdenadaPorScoreDecrescenteComEmpatesENumeroEstavel` |
| cadastro de usuário com hash de senha | `UsuarioServiceTest › cadastraComSenhaEmHash` (com BCrypt real, não mock) |
| login com credenciais válidas | `AuthServiceTest › credenciaisValidasDevolvemTokenComSubIgualAoEmail` |
| login com credenciais inválidas | `senhaErradaNaoDevolveToken`, `emailInexistenteNaoDevolveToken` |
| geração e validação de JWT (claim `sub` = e-mail) | `JwtUtilTest` (7 casos: válido, `sub`, e-mail extraído, adulterado, expirado, outra chave, malformado) |

### Casos cobertos além do que a Seção 20.2 lista

- e-mail já cadastrado (`emailJaCadastradoFalhaSemSalvar`) e cadastro simultâneo barrado pelo índice único (`cadastroSimultaneoBarradoPeloIndiceUnicoViraEmailJaCadastrado`);
- nome com caixa diferente é outro treino (`nomeComCaixaDiferenteEhOutroTreino`) e cadastro simultâneo do mesmo nome (`cadastroSimultaneoDoMesmoNomeDevolveOQueFoiGravado`);
- treino e comentário inexistentes lançam a exceção certa em cada operação;
- conflito de versão ao regravar o treino é retentado (`conflitoDeVersaoRelêOTreinoETentaDeNovo`) e desiste após as tentativas (`conflitoPersistenteDesisteAposAsTentativas`);
- nenhum método público de `TreinoService`/`ComentarioService` recebe ou devolve Model (`nenhumMetodoPublicoExpoeModelOuId`).

## Resultado

Nenhum caso listado nas Seções 20.1 e 20.2 está sem teste correspondente.
