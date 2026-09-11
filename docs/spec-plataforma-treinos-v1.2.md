# SPEC DEVELOPMENT
## Plataforma de Compartilhamento de Treinos — ODS 3: Saúde e Bem-Estar
**Versão:** 1.2
**Tipo:** Especificação de Desenvolvimento — Backend
**Tecnologia principal:** Java + Spring Boot + Spring Security (JWT)
**Banco de dados:** MongoDB
**Documentação da API:** Swagger / OpenAPI

---

## Changelog v1.1 → v1.2

Troca de banco de dados de **PostgreSQL (relacional) para MongoDB (NoSQL)** — decisão didática: a modelagem deve refletir as características próprias do NoSQL, não apenas trocar o driver por baixo de um desenho relacional. Nenhuma funcionalidade de escopo mudou; mudaram modelagem de dados e parte do contrato da API:

* **`Comentario` deixa de ser entidade/coleção própria** e passa a ser uma estrutura **embutida** dentro do documento `Treino` (composição, não relação por chave estrangeira) — reflete a característica de agregado do MongoDB.
* **Nenhuma camada da aplicação manipula o id gerado pelo banco.** O `_id` do MongoDB existe só como detalhe interno de persistência; Controller, Service e Model nunca o leem, gravam ou expõem. Toda referência externa (URL, DTO, claim do JWT) usa um campo de negócio já único:
  * `Treino` é referenciado por **`nome`** (já era a chave de unicidade global — Seção 5).
  * `Comentario` é referenciado por **`numero`** — um inteiro sequencial atribuído no momento da criação, escopado ao treino ao qual pertence, imutável e independente da ordem de exibição por `score`.
  * `Usuario` é referenciado por **`email`** (já era o identificador de login — Seção 4.1), inclusive como claim `sub` do token JWT.
* **Autor de `Treino`/`Comentario`** passa a ser um recorte denormalizado (`autorNome`, `autorEmail`) gravado no momento da criação, em vez de uma relação `@ManyToOne` para `Usuario` — evita duplicar o hash de senha em outros documentos.
* **`avaliacoes`** (mapa de like/dislike dentro de `Comentario`) muda a chave de `usuarioId` (Long) para **`email`** (String) do usuário que votou — mesma razão de não expor id.
* **Seção 23 da v1.1 (ponto em aberto sobre GET exigir login) fica resolvida e incorporada como regra definitiva na Seção 8.2** — deixa de ser uma decisão assumida/pendente de confirmação.
* Endpoints de like/dislike deixam de ser um recurso top-level (`/comentarios/{id}/like`) e passam a ser aninhados sob o treino: `/treinos/{nome}/comentarios/{numero}/like` — consequência direta de `Comentario` não ser mais uma coleção própria.

Nada do que está descrito abaixo foi implementado em código antes desta revisão — a v1.2 substitui a v1.1 como referência única para o desenvolvimento.

---

## Changelog v1.0 → v1.1 (histórico)

* **Autenticação definida:** JWT via Spring Security. O usuário autor de cada ação (cadastrar treino, comentar, avaliar) é extraído do token, não mais informado no corpo da requisição.
* **Avaliação (Like/Dislike) redesenhada:** uma avaliação por usuário por comentário; trocar o voto substitui o anterior (upsert).
* **Atributos de `Usuario` fechados:** `nome`, `email`, `senha` (hash).
* **Endpoints de cadastro e login adicionados.**
* **Unicidade de treino esclarecida:** verificação por `nome`, global, case-sensitive, comparação exata.
* **Sem paginação** em nenhum endpoint.
* **Tratamento de erro:** sem contrato de exceção global padronizado.
* **Retorno de treino já existente:** HTTP 200 simples.
* **`dataCriacao`** adicionado a `Treino` e `Comentario` como campo de auditoria.

---

# 1. Visão Geral

O sistema consiste em uma **API REST para compartilhamento de treinos e experiências de usuários**, desenvolvida no contexto da **ODS 3 — Saúde e Bem-Estar**.

A aplicação permite que usuários autenticados:

1. Se cadastrem e façam login (JWT);
2. Cadastrem treinos;
3. Consultem treinos existentes;
4. Compartilhem experiências por meio de comentários;
5. Avaliem comentários utilizando **Like** ou **Dislike** (um voto por usuário, substituível);
6. Consultem os comentários de um treino ordenados de acordo com sua avaliação.

A proposta é criar uma base colaborativa na qual as experiências dos usuários auxiliem outras pessoas na escolha e compreensão dos treinos.

O escopo está deliberadamente limitado a essas funcionalidades. O sistema **não terá funcionalidades de acompanhamento de progresso, metas, planos de treino, métricas corporais ou avaliação do treino em si**.

---

# 2. Objetivo

Desenvolver um backend capaz de disponibilizar uma plataforma colaborativa de informações sobre treinos, permitindo que os usuários compartilhem suas experiências e que a própria comunidade determine a relevância dos comentários por meio de avaliações.

O projeto está relacionado à **ODS 3 — Saúde e Bem-Estar**, utilizando a prática de exercícios físicos como domínio da aplicação. A escolha de um banco NoSQL orientado a documentos (MongoDB) é também um objetivo didático em si: modelar o mesmo domínio de forma coerente com agregados e composição, em vez de tabelas normalizadas.

---

# 3. Escopo

## 3.1 Funcionalidades contempladas

* Cadastro de usuário;
* Autenticação (login) e emissão de token JWT;
* Cadastro de treino;
* Verificação de existência de treino (interna ao cadastro);
* Consulta de treinos;
* Consulta de um treino específico (por `nome`);
* Cadastro de comentário;
* Consulta dos comentários de um treino;
* Like em comentário;
* Dislike em comentário;
* Troca de avaliação (like → dislike ou vice-versa);
* Ranking dos comentários;
* Documentação da API com Swagger/OpenAPI.

## 3.2 Funcionalidades fora do escopo

Não fazem parte desta versão:

* Edição de comentários;
* Exclusão de comentários;
* Edição ou acompanhamento de progresso do treino;
* Avaliação por estrelas;
* Avaliação do treino;
* Planos personalizados;
* Metas individuais;
* Métricas de desempenho;
* Rede social ou mensagens privadas;
* Paginação de resultados;
* Recuperação de senha / refresh token;
* Frontend.

O projeto será desenvolvido **exclusivamente como backend**.

---

# 4. Domínio da Aplicação

O domínio possui duas coleções persistidas de fato (`Usuario`, `Treino`), mais duas estruturas **embutidas** — `Comentario` dentro de `Treino`, e o mapa de avaliações dentro de `Comentario`. Nenhuma delas é uma coleção/entidade própria:

```text
USUÁRIO (coleção própria)
   │
   │ referenciado por (autorEmail/autorNome, denormalizado)
   ▼
TREINO (coleção própria)
   │
   │ contém (composição — lista embutida)
   ▼
COMENTÁRIO (embutido em Treino, endereçado por numero sequencial)
   │
   │ contém (mapa email → tipo, 1 entrada por usuário)
   ▼
AVALIAÇÃO (LIKE | DISLIKE, embutida em Comentario)
```

Um usuário pode:

* cadastrar treinos;
* comentar em treinos;
* avaliar comentários (uma avaliação por comentário; pode trocar de tipo a qualquer momento).

**Princípio de endereçamento (novo na v1.2):** nenhuma camada da aplicação — Controller, Service ou Model — lê, grava ou expõe o id gerado pelo MongoDB. Toda referência externa usa um campo de negócio já único: `nome` (Treino), `numero` (Comentario, escopado ao treino) e `email` (Usuario).

## 4.1 Usuário

Representa uma pessoa que utiliza a plataforma, autenticada via JWT. É a única entidade que continua sendo referenciada por um identificador natural em todos os contextos — `email` — nunca por id técnico.

### Atributos

```text
Usuario
├── id       (interno — gerado pelo MongoDB, nunca lido/gravado pela aplicação)
├── nome
├── email
└── senha    (armazenada como hash, via Spring Security / PasswordEncoder)
```

`email` é o identificador de login **e** o identificador usado em toda a aplicação (inclusive como claim `sub` do JWT — Seção 8.1).

---

# 5. Treino

O treino é a **entidade central do sistema**. É compartilhado por todos os usuários: qualquer usuário autenticado pode consultar qualquer treino cadastrado por qualquer outro usuário.

### Atributos

```text
Treino
├── id            (interno — gerado pelo MongoDB, nunca lido/gravado pela aplicação)
├── nome          (identificador de negócio — único globalmente, usado nas URLs)
├── descricao
├── autorNome     (recorte denormalizado do Usuario que cadastrou)
├── autorEmail    (recorte denormalizado do Usuario que cadastrou)
├── dataCriacao   (auditoria — não participa de nenhuma regra de negócio)
└── comentarios   (lista embutida de Comentario — composição, não referência)
```

`Treino` não guarda uma referência (`@ManyToOne`/`@DBRef`) para `Usuario` inteiro — guarda só o recorte necessário para exibição (`autorNome`, `autorEmail`). Isso evita duplicar o hash de senha em cada treino cadastrado.

### Regra de negócio — não duplicidade

Antes de cadastrar um treino, a aplicação verifica se já existe um treino com o **mesmo nome**, em escopo **global** (independente de quem o cadastrou):

* comparação **exata** e **case-sensitive** (`"Treino de Perna"` ≠ `"treino de perna"`);
* não há camada de normalização, similaridade ou "fuzzy match";
* decisão consciente: o sistema privilegia simplicidade de implementação sobre curadoria semântica de duplicados.

```text
Solicitação de cadastro
        │
        ▼
existsByNome(nome)?
   │           │
  SIM         NÃO
   │           │
   ▼           ▼
Retorna       Cria
existente     treino
(HTTP 200)   (HTTP 201)
```

Em ambos os casos a resposta é o `TreinoResponseDTO` correspondente — não há status HTTP diferenciado para "já existia" (não se usa 409 Conflict).

---

# 6. Comentário

O comentário representa a **experiência de um usuário em relação a um treino**. Na v1.2, `Comentario` **não é uma coleção própria** — é uma estrutura embutida na lista `comentarios` do documento `Treino` ao qual pertence (composição/agregado, característica idiomática do MongoDB).

### Atributos

```text
Comentario
├── numero        (sequencial dentro do treino — 1º, 2º, 3º comentário daquele treino;
│                  atribuído na criação, imutável, independente da ordem de exibição por score)
├── texto
├── autorNome     (recorte denormalizado do Usuario que comentou)
├── autorEmail    (recorte denormalizado do Usuario que comentou)
├── dataCriacao                          (auditoria)
└── avaliacoes: Map<email, TipoAvaliacao> (ver Seção 7 — não é uma entidade própria)
```

`numero` é calculado no momento da criação como `(maior numero já usado naquele treino) + 1`, começando em 1. É essa a chave usada para localizar um comentário específico dentro do treino (endpoints de like/dislike — Seção 15) — nunca um id técnico.

## 6.1 Imutabilidade

Depois de criado, o **texto** de um comentário:

* não pode ser alterado;
* não pode ser excluído.

Isso preserva o histórico das experiências compartilhadas.

A única coisa que pode mudar após a criação é o conjunto de avaliações (`avaliacoes`) — e mesmo assim, só por meio de novos votos de like/dislike, nunca editando o texto.

---

# 7. Sistema de Avaliação

## 7.1 Regra de negócio

Cada comentário admite **no máximo uma avaliação por usuário**:

* o usuário registra Like ou Dislike via `POST`;
* se o usuário **ainda não** avaliou aquele comentário, a avaliação é criada;
* se o usuário **já** avaliou aquele comentário (com qualquer um dos dois tipos), o novo voto **substitui** o anterior — não há acúmulo de avaliações do mesmo usuário no mesmo comentário;
* não existe operação de "remover avaliação" nesta versão — trocar de tipo é a única forma de alteração.

## 7.2 Modelagem

Estrutura de dados, sem entidade JPA/coleção independente:

```java
public enum TipoAvaliacao {
    LIKE,
    DISLIKE
}
```

```java
// dentro de Comentario (estrutura embutida em Treino, não é @Document)
private Map<String, TipoAvaliacao> avaliacoes = new HashMap<>();
// chave: email do usuário que votou (não o id técnico)
```

A chave do mapa (`email`) garante, por construção, no máximo uma avaliação por usuário — não é necessária constraint de unicidade adicional nem lógica de "buscar se existe, senão criar". No MongoDB, esse mapa é serializado automaticamente como um sub-documento aninhado dentro do `Comentario`, que por sua vez está aninhado dentro do `Treino` — não existe tabela auxiliar como `comentario_avaliacao` (essa era uma necessidade específica de JPA/relacional, Seção 18 da v1.1).

**Nota de implementação (adicionada em 2026-09-09, durante a F01-T04):** o MongoDB não aceita **ponto em nome de campo** de sub-documento, e todo e-mail tem ponto — então gravar `avaliacoes` com a chave crua falha com *"Map key ... contains dots but no replacement was configured"*. A solução adotada mantém a modelagem desta seção intacta: o `MappingMongoConverter` é configurado com `setMapKeyDotReplacement("[dot]")` (classe `MongoConfig`), que troca o ponto na gravação e desfaz a troca na leitura. Em Java a chave continua sendo o e-mail real; no banco ela aparece como `ana@exemplo[dot]com`. A sequência `[dot]` foi escolhida por não ser válida em endereço de e-mail — um substituto comum como `_` corromperia `joao_silva@gmail.com`, que voltaria do banco como `joao.silva@gmail.com`. Vale só para **chaves** de mapa: campos que guardam e-mail como *valor* (`autorEmail`) seguem com o ponto normal.


Comportamento de domínio (no próprio Model `Comentario`):

```java
public void registrarAvaliacao(String email, TipoAvaliacao tipo) {
    avaliacoes.put(email, tipo); // upsert natural do Map
}

public int calcularScore() {
    long likes = avaliacoes.values().stream()
        .filter(t -> t == TipoAvaliacao.LIKE).count();
    long dislikes = avaliacoes.values().stream()
        .filter(t -> t == TipoAvaliacao.DISLIKE).count();
    return (int) (likes - dislikes);
}
```

## 7.3 Ranking

```text
score = quantidade de LIKE - quantidade de DISLIKE
```

### Exemplo

| Comentário   | Likes | Dislikes | Score |
| ------------ | ----: | -------: | ----: |
| Comentário A |    50 |        5 |    45 |
| Comentário B |    30 |        8 |    22 |
| Comentário C |    10 |       15 |    -5 |

Os comentários devem ser apresentados em **ordem decrescente de score**. Como `Comentario` está embutido em `Treino`, essa ordenação é feita em memória sobre a lista já carregada (`treino.getComentarios()`), não por uma query de repository separada.

O `score` continua sendo um **dado derivado**, calculado a partir de `avaliacoes` — não é uma coluna/campo persistido nem uma entidade independente. Importante: `score` determina a *ordem de exibição*, mas **não** o `numero` de cada comentário — `numero` é fixo desde a criação e nunca muda, mesmo que a posição na lista ordenada mude a cada novo voto.

---

# 8. Autenticação e Segurança

## 8.1 Mecanismo

* **Spring Security** + **JWT**.
* Login via `email` + `senha`; sucesso retorna um token JWT.
* Toda requisição a endpoints protegidos deve enviar `Authorization: Bearer <token>`.
* O claim `sub` do token JWT é o **`email`** do usuário autenticado — nunca o id técnico gerado pelo MongoDB. É esse email que o filtro de segurança extrai e disponibiliza para Controller/Service.
* O `email` usado para: cadastrar treino, cadastrar comentário e registrar avaliação **vem do token**, extraído no filtro de segurança — nunca é informado no corpo (`RequestDTO`) pelo cliente.
* Senhas são armazenadas com hash (`PasswordEncoder` do Spring Security — ex.: BCrypt).

**Trade-off consciente (aceito por ser projeto didático):** por o JWT não ser criptografado (apenas assinado — o payload é legível por qualquer um que decodifique o base64), usar `email` como claim expõe um dado real da pessoa caso o token vaze, em vez de um id opaco. É uma escolha coerente com o princípio de não usar ids técnicos em lugar nenhum da aplicação; mitigável no futuro (token de vida curta, HTTPS, não logar o header `Authorization`) caso o projeto saia do escopo didático.

## 8.2 Endpoints públicos vs. protegidos

* **Públicos** (não exigem token): `POST /usuarios` (cadastro), `POST /login`.
* **Protegidos** (exigem token válido): **todos os demais, inclusive todos os `GET`s** — consulta de treinos, consulta de comentários, like/dislike.

Esta é uma **regra definitiva** desta especificação (consolidada a partir da v1.1, onde era uma decisão assumida pendente de confirmação): toda a API, exceto cadastro e login, exige autenticação, por consistência e simplicidade de implementação. Não há modo de consulta anônima nesta versão.

---

# 9. Arquitetura

A aplicação seguirá arquitetura em camadas baseada no padrão:

```text
Cliente
   │
   ▼
Filtro JWT (Spring Security)
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
MongoDB
```

O domínio será representado pelos Models e os dados de entrada e saída da API serão transportados por DTOs.

```text
                  CLIENTE
                     │
                     ▼
              ┌─────────────┐
              │  Filtro JWT │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │ Controller  │
              └──────┬──────┘
                     │
                   DTO
                     │
                     ▼
              ┌─────────────┐
              │   Service   │
              └──────┬──────┘
                     │
                   Model
                     │
                     ▼
              ┌─────────────┐
              │ Repository  │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │   MongoDB   │
              └─────────────┘
```

---

# 10. Tecnologias

## Backend

* Java;
* Spring Boot;
* Spring Web;
* **Spring Data MongoDB**;
* **Spring Security** (autenticação/autorização);
* **JJWT** (ou biblioteca equivalente de emissão/validação de JWT);
* Bean Validation.

## Banco de dados

* **MongoDB.**

## Documentação

* Swagger;
* OpenAPI (com esquema de segurança `bearerAuth` configurado).

## Testes

* JUnit;
* Mockito;
* testes externos da API.

---

# 11. Estrutura do Projeto

```text
src/
└── main/
    └── java/
        └── com.projeto.ods3/
            │
            ├── controller/
            │   ├── UsuarioController      (cadastro)
            │   ├── AuthController         (login)
            │   ├── TreinoController
            │   └── ComentarioController
            │
            ├── service/
            │   ├── UsuarioService
            │   ├── AuthService             (autenticação, geração de token)
            │   ├── TreinoService
            │   └── ComentarioService
            │
            ├── repository/
            │   ├── UsuarioRepository
            │   └── TreinoRepository
            │
            ├── model/
            │   ├── Usuario
            │   ├── Treino
            │   ├── Comentario             (estrutura embutida, não é @Document)
            │   └── TipoAvaliacao (enum)
            │
            ├── security/
            │   ├── JwtFilter
            │   ├── JwtUtil
            │   └── SecurityConfig
            │
            ├── dto/
            │   ├── usuario/
            │   ├── auth/                   (LoginRequestDTO, LoginResponseDTO)
            │   ├── treino/
            │   └── comentario/
            │
            └── exception/
```

Não há `ComentarioRepository` nem `AvaliacaoRepository` — `Comentario` é responsabilidade do `TreinoRepository`/`TreinoService` (persiste junto com o `Treino`), e avaliação é responsabilidade do `ComentarioService` e do Model `Comentario`.

---

# 12. Model

```text
Usuario        (@Document — coleção própria)
Treino         (@Document — coleção própria, com lista embutida de Comentario)
Comentario     (estrutura embutida, não é @Document)
TipoAvaliacao  (enum: LIKE, DISLIKE)
```

Relações:

```text
Usuario ── (autorEmail/autorNome, denormalizado) ── Treino
Usuario ── (autorEmail/autorNome, denormalizado) ── Comentario
Treino  1 ─── N Comentario   (composição — lista embutida, não FK)
Comentario 1 ─── N Avaliacao (Map interno, não é entidade própria)
```

---

# 13. Repository

```text
UsuarioRepository extends MongoRepository<Usuario, String>
TreinoRepository  extends MongoRepository<Treino, String>
```

Responsabilidade exclusiva: comunicação com a camada de persistência. **Não contém regras de negócio.**

Métodos convencionais utilizados:

```text
save()
findAll()
existsByNome()       // TreinoRepository — verificação de duplicidade
findByNome()         // TreinoRepository — busca por nome (endpoint /treinos/{nome})
findByEmail()        // UsuarioRepository — login e resolução do autor via token
existsByEmail()      // UsuarioRepository — validação de unicidade no cadastro
```

Não existe `findById` usado pela camada de negócio — toda busca externa é por campo de negócio (`nome`, `email`). Não há `ComentarioRepository`: o mapa de avaliações e a lista de comentários são persistidos automaticamente junto com `Treino`.

---

# 14. Service

### `UsuarioService`

* cadastra usuário (aplica hash na senha antes de salvar);
* valida unicidade de e-mail antes de cadastrar.

### `AuthService`

* autentica `email` + `senha`;
* gera o token JWT (claim `sub` = `email`) em caso de sucesso.

### `TreinoService`

* verifica existência do treino (por `nome`, global, case-sensitive, exato);
* cria treino, associando `autorNome`/`autorEmail` a partir do usuário autenticado;
* recupera treino(s) — listagem completa e busca individual por `nome`.

### `ComentarioService`

* carrega o `Treino` (por `nome`), cria o `Comentario` com o próximo `numero` sequencial daquele treino, associa `autorNome`/`autorEmail`, adiciona à lista embutida e salva o `Treino`;
* registra avaliação (like/dislike): carrega o `Treino`, localiza o `Comentario` na lista embutida pelo `numero`, delega ao Model (`comentario.registrarAvaliacao(email, tipo)`) e salva o `Treino` de volta;
* recupera os comentários de um treino (carregando o `Treino` por `nome`), ordenados por `calcularScore()` decrescente — ordenação em memória, não uma query separada.

A camada Service é o principal ponto de concentração da lógica da aplicação. A camada Model concentra o comportamento próprio da entidade (registrar avaliação, calcular score).

---

# 15. Controller

```text
POST   /usuarios                                     (cadastro — público)
POST   /login                                        (autenticação — público)

POST   /treinos                                      (protegido)
GET    /treinos                                      (protegido)
GET    /treinos/{nome}                               (protegido)

POST   /treinos/{nome}/comentarios                   (protegido)
GET    /treinos/{nome}/comentarios                   (protegido)

POST   /treinos/{nome}/comentarios/{numero}/like     (protegido)
POST   /treinos/{nome}/comentarios/{numero}/dislike  (protegido)
```

O Controller deve:

1. receber a requisição;
2. validar/receber o DTO;
3. encaminhar a operação ao Service (o `email` do autor, quando necessário, vem do contexto de segurança/token — não do DTO);
4. retornar a resposta HTTP adequada.

O Controller **não deve implementar regras de negócio**.

---

# 16. DTOs

```text
UsuarioRequestDTO       (nome, email, senha)
UsuarioResponseDTO      (id, nome, email)                          // id: String (Mongo)

LoginRequestDTO         (email, senha)
LoginResponseDTO        (token)

TreinoRequestDTO        (nome, descricao)                          // sem autor — vem do token
TreinoResponseDTO       (nome, descricao, autorNome, autorEmail, dataCriacao)

ComentarioRequestDTO    (texto)                                    // sem autor/treino no corpo
ComentarioResponseDTO   (numero, texto, autorNome, autorEmail, dataCriacao, likes, dislikes, score)
```

`TreinoResponseDTO` e `ComentarioResponseDTO` não expõem `id` técnico — `Treino` já se identifica por `nome` (que o DTO já carrega) e `Comentario` por `numero`. `UsuarioResponseDTO` é a única exceção: mantém `id` (String) só como referência de exibição, já que `Usuario` não é endereçado por id em nenhum endpoint (é endereçado por `email` no login e no token).

Não há `AvaliacaoRequestDTO` — os endpoints de like/dislike não recebem corpo; o comentário (via `numero` na URL) e o usuário (via token) já identificam a operação.

Fluxo de entrada:

```text
JSON → RequestDTO → Service → Model → Repository
```

Fluxo de saída:

```text
Repository → Model → Service → ResponseDTO → JSON
```

A API não deve expor diretamente as entidades JPA/documentos como contrato público, e a `senha` nunca aparece em nenhum `ResponseDTO`.

---

# 17. API REST

## Autenticação

```http
POST /usuarios        # cadastro
POST /login            # autenticação, retorna token JWT
```

## Treinos

```http
POST /treinos
GET /treinos
GET /treinos/{nome}
```

## Comentários

```http
POST /treinos/{nome}/comentarios
GET /treinos/{nome}/comentarios
```

## Avaliações

```http
POST /treinos/{nome}/comentarios/{numero}/like
POST /treinos/{nome}/comentarios/{numero}/dislike
```

Todos os endpoints exceto `/usuarios` e `/login` exigem cabeçalho `Authorization: Bearer <token>`.

Não há paginação em nenhum endpoint de listagem — todos os registros são retornados de uma vez, por decisão explícita de escopo.

Os endpoints devem utilizar códigos HTTP semanticamente adequados para sucesso, validação e erros de negócio, com a exceção explícita já registrada: retorno de treino já existente usa `200 OK`, não um status de conflito.

---

# 18. MongoDB

Estrutura conceitual — duas coleções, sem tabelas auxiliares:

```text
usuarios
├── _id (ObjectId, interno)
├── nome
├── email    (índice único)
└── senha (hash)

treinos
├── _id (ObjectId, interno)
├── nome        (índice único)
├── descricao
├── autorNome
├── autorEmail
├── dataCriacao
└── comentarios: [                          ← array embutido, não coleção própria
      {
        numero, texto, autorNome, autorEmail, dataCriacao,
        avaliacoes: { "email1": "LIKE", "email2": "DISLIKE", ... }  ← sub-documento embutido
      },
      ...
    ]
```

Não existem `_id` próprios para os itens de `comentarios` nem para as entradas de `avaliacoes` — ambos são estruturas embutidas dentro do documento `Treino`, endereçadas por `numero` (comentário) e `email` (avaliação), nunca por um id técnico. Isso substitui por completo o desenho relacional da v1.1 (tabelas `usuario`, `treino`, `comentario`, `comentario_avaliacao` com chaves estrangeiras).

As chaves de `avaliacoes` são gravadas com o ponto escapado (`"email1@dominio[dot]com"`) — ver nota de implementação na Seção 7.2.

Índices únicos necessários:

* `email` na coleção `usuarios`;
* `nome` na coleção `treinos`.

---

# 19. Swagger / OpenAPI

A API deverá ser documentada utilizando Swagger/OpenAPI, incluindo:

* esquema de segurança `bearerAuth` (JWT), para autenticar chamadas de endpoints protegidos direto pela interface do Swagger;
* endpoints, parâmetros, DTOs, respostas e códigos HTTP retornados — incluindo os path variables `{nome}` e `{numero}`.

O Swagger será também utilizado como ferramenta de exploração manual da API durante o desenvolvimento.

---

# 20. Estratégia de Testes

## 20.1 Model — testes unitários

```text
Comentario.registrarAvaliacao()  — cria avaliação nova; substitui avaliação existente do mesmo email
Comentario.calcularScore()       — likes - dislikes, incluindo casos de empate e score negativo
Treino / Usuario                 — regras próprias da entidade, se houver
```

## 20.2 Service — testes unitários (JUnit + Mockito)

```text
TreinoService
ComentarioService
UsuarioService
AuthService
```

Casos importantes:

* treino existente (retorna existente, não duplica);
* treino inexistente (cria novo);
* criação de comentário — `numero` atribuído corretamente (sequencial, começando em 1);
* like em comentário sem avaliação prévia do usuário;
* troca de like para dislike do mesmo usuário (e vice-versa) — confirma substituição, não acúmulo;
* cálculo do score;
* ordenação dos comentários por score decrescente — sem afetar o `numero` de cada um;
* cadastro de usuário com hash de senha;
* login com credenciais válidas/inválidas;
* geração e validação de token JWT (claim `sub` = email).

## 20.3 Controller — testes externos

Não serão realizados testes unitários dos Controllers. Validação por requisições HTTP reais, considerando: endpoint, método, payload, cabeçalho de autenticação, status HTTP, resposta.

## 20.4 Repository

Sem testes unitários para os Repositories convencionais — confia-se nas abstrações do Spring Data MongoDB.

---

# 21. Critérios de Aceitação

* [ ] Um usuário puder ser cadastrado (com senha em hash);
* [ ] Um usuário puder autenticar e obter um token JWT com claim `sub` = email;
* [ ] Endpoints protegidos rejeitarem requisições sem token válido;
* [ ] Um usuário autenticado puder cadastrar um treino;
* [ ] A API verificar a existência do treino (nome, global, case-sensitive) antes de criá-lo;
* [ ] Um treino existente for retornado com HTTP 200 em vez de duplicado;
* [ ] Um treino puder ser consultado individualmente pelo `nome`, sem uso de id técnico na URL;
* [ ] Um treino puder receber múltiplos comentários, cada um com `numero` sequencial próprio;
* [ ] Comentários não puderem ter o texto alterado ou excluído;
* [ ] Um usuário puder dar Like ou Dislike em um comentário identificado por `nome` do treino + `numero`;
* [ ] Um usuário puder trocar seu voto (like ↔ dislike) e apenas o voto mais recente contar;
* [ ] O score for determinado por `likes - dislikes` calculado a partir das avaliações vigentes;
* [ ] Os comentários forem retornados por ordem decrescente de score, sem que isso altere o `numero` de nenhum;
* [ ] Nenhum endpoint de listagem aplicar paginação;
* [ ] Os dados forem persistidos no MongoDB, com `Comentario` embutido em `Treino`;
* [ ] Nenhum endpoint da API aceitar ou expor um id técnico (Mongo `_id`) em request ou response;
* [ ] A API estiver documentada no Swagger com suporte a Bearer JWT;
* [ ] Models possuírem testes unitários (incluindo `registrarAvaliacao`/`calcularScore`);
* [ ] Services possuírem testes unitários;
* [ ] Controllers forem validados por testes externos;
* [ ] Repositories utilizem as abstrações do Spring Data MongoDB.

---

# 22. Princípio Arquitetural

> **Filtro JWT autentica a requisição (por email, não por id).
> Controller expõe a API (endereçada por nome/numero/email, nunca por id técnico).
> DTO define o contrato de comunicação.
> Service implementa as regras de negócio.
> Model representa o domínio (inclusive o comportamento de avaliação/score).
> Repository realiza a persistência.
> MongoDB armazena os dados, com Comentario embutido em Treino.
> Swagger documenta a API.
> Testes unitários validam o domínio e as regras de negócio.
> Testes externos validam a API.**

Esse é o **SPEC DEVELOPMENT v1.2** da aplicação.
