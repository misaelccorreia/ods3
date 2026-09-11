package com.projeto.ods3.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projeto.ods3.dto.auth.LoginRequestDTO;
import com.projeto.ods3.dto.auth.LoginResponseDTO;
import com.projeto.ods3.dto.comentario.ComentarioRequestDTO;
import com.projeto.ods3.dto.comentario.ComentarioResponseDTO;
import com.projeto.ods3.dto.treino.TreinoRequestDTO;
import com.projeto.ods3.dto.treino.TreinoResponseDTO;
import com.projeto.ods3.dto.usuario.UsuarioRequestDTO;
import com.projeto.ods3.dto.usuario.UsuarioResponseDTO;
import com.projeto.ods3.model.Treino;
import com.projeto.ods3.model.Usuario;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes externos da API (Seção 20.3): requisições HTTP reais contra a aplicação rodando numa porta
 * aleatória, sem mocks. Usa um banco próprio para não tocar nos dados de desenvolvimento.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.mongodb.uri=mongodb://localhost:27017/ods3_teste_api")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiExternaTest {

    private static final String SENHA = "segredo123";

    @Value("${local.server.port}")
    int porta;

    @Autowired
    MongoTemplate mongo;

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void limparDados() {
        // remove os documentos mas mantém as coleções e os índices únicos criados na subida
        mongo.remove(new Query(), Usuario.class);
        mongo.remove(new Query(), Treino.class);
    }

    @AfterAll
    void apagarBancoDeTeste() {
        mongo.getDb().drop();
    }

    // =====================================================================================
    // Usuários
    // =====================================================================================

    @Test
    void cadastroDeUsuarioDevolve201SemSenhaNemId() {
        HttpResponse<String> r = post("/usuarios", new UsuarioRequestDTO("Ana", "ana@exemplo.com", SENHA), null);

        assertEquals(201, r.statusCode());
        UsuarioResponseDTO dto = ler(r, UsuarioResponseDTO.class);
        assertEquals("Ana", dto.nome());
        assertEquals("ana@exemplo.com", dto.email());
        assertSemCamposTecnicos(r);
    }

    @Test
    void senhaFicaGravadaComoHashNoBanco() {
        post("/usuarios", new UsuarioRequestDTO("Ana", "ana@exemplo.com", SENHA), null);

        String gravada = mongo.getCollection("usuarios").find(new Document("email", "ana@exemplo.com")).first().getString("senha");
        assertNotEquals(SENHA, gravada);
        assertTrue(gravada.startsWith("$2"), "esperado hash BCrypt, veio: " + gravada);
    }

    @Test
    void cadastroComEmailRepetidoDevolve409() {
        post("/usuarios", new UsuarioRequestDTO("Ana", "ana@exemplo.com", SENHA), null);
        HttpResponse<String> r = post("/usuarios", new UsuarioRequestDTO("Outra Ana", "ana@exemplo.com", "outra123"), null);

        assertEquals(409, r.statusCode());
        assertTrue(detalhe(r).contains("ana@exemplo.com"), r.body());
    }

    @Test
    void cadastroInvalidoDevolve400() {
        assertEquals(400, post("/usuarios", new UsuarioRequestDTO("", "nao-eh-email", ""), null).statusCode());
        assertEquals(400, postCru("/usuarios", "{isto nao e json", null).statusCode());
    }

    // =====================================================================================
    // Login e segurança
    // =====================================================================================

    @Test
    void loginValidoDevolveTokenQueAbreRotasProtegidas() {
        post("/usuarios", new UsuarioRequestDTO("Ana", "ana@exemplo.com", SENHA), null);

        HttpResponse<String> r = post("/login", new LoginRequestDTO("ana@exemplo.com", SENHA), null);

        assertEquals(200, r.statusCode());
        String token = ler(r, LoginResponseDTO.class).token();
        assertEquals(200, get("/treinos", token).statusCode());
    }

    @Test
    void tokenTemSubIgualAoEmail() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"sub\":\"ana@exemplo.com\""), payload);
    }

    @Test
    void loginComSenhaErradaOuEmailInexistenteDevolve401SemToken() {
        post("/usuarios", new UsuarioRequestDTO("Ana", "ana@exemplo.com", SENHA), null);

        HttpResponse<String> senhaErrada = post("/login", new LoginRequestDTO("ana@exemplo.com", "errada"), null);
        HttpResponse<String> emailInexistente = post("/login", new LoginRequestDTO("ninguem@exemplo.com", SENHA), null);

        assertEquals(401, senhaErrada.statusCode());
        assertEquals(401, emailInexistente.statusCode());
        assertFalse(senhaErrada.body().contains("token"));
        assertEquals(detalhe(senhaErrada), detalhe(emailInexistente), "mensagem nao pode revelar se o e-mail existe");
    }

    @Test
    void loginInvalidoDevolve400() {
        assertEquals(400, post("/login", new LoginRequestDTO("", ""), null).statusCode());
    }

    @Test
    void rotasProtegidasSemTokenOuComTokenInvalidoDevolvem401() {
        String nome = segmento("Treino de Perna");
        for (String[] rota : new String[][]{
                {"GET", "/treinos"}, {"POST", "/treinos"}, {"GET", "/treinos/" + nome},
                {"GET", "/treinos/" + nome + "/comentarios"}, {"POST", "/treinos/" + nome + "/comentarios"},
                {"POST", "/treinos/" + nome + "/comentarios/1/like"}, {"POST", "/treinos/" + nome + "/comentarios/1/dislike"}}) {
            assertEquals(401, enviar(rota[0], rota[1], "{}", null).statusCode(), "sem token: " + rota[0] + " " + rota[1]);
            assertEquals(401, enviar(rota[0], rota[1], "{}", "token.invalido.qualquer").statusCode(), "token invalido: " + rota[1]);
        }
    }

    // =====================================================================================
    // Treinos
    // =====================================================================================

    @Test
    void cadastroDeTreinoNovoDevolve201ComAutorDoTokenELocation() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        HttpResponse<String> r = post("/treinos", new TreinoRequestDTO("Glúteo e Posterior", "Stiff e elevação pélvica"), token);

        assertEquals(201, r.statusCode());
        TreinoResponseDTO dto = ler(r, TreinoResponseDTO.class);
        assertEquals("Ana", dto.autorNome());
        assertEquals("ana@exemplo.com", dto.autorEmail());
        assertNotNull(dto.dataCriacao());
        assertSemCamposTecnicos(r);

        String location = r.headers().firstValue("Location").orElseThrow();
        assertEquals(200, get(location, token).statusCode(), "Location deve apontar para o treino criado");
    }

    @Test
    void cadastroDeTreinoRepetidoDevolve200ComOExistenteSemDuplicar() {
        String ana = novoUsuario("Ana", "ana@exemplo.com");
        String bruno = novoUsuario("Bruno", "bruno@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Treino de Perna", "original"), ana);

        HttpResponse<String> r = post("/treinos", new TreinoRequestDTO("Treino de Perna", "tentativa do Bruno"), bruno);

        assertEquals(200, r.statusCode(), "repetido e 200, nao 201 nem 409");
        assertEquals("original", ler(r, TreinoResponseDTO.class).descricao());
        assertEquals("ana@exemplo.com", ler(r, TreinoResponseDTO.class).autorEmail());
        assertEquals(1, mongo.getCollection("treinos").countDocuments());
    }

    @Test
    void nomeComCaixaDiferenteCriaOutroTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        assertEquals(201, post("/treinos", new TreinoRequestDTO("Treino de Perna", "a"), token).statusCode());
        assertEquals(201, post("/treinos", new TreinoRequestDTO("treino de perna", "b"), token).statusCode());
        assertEquals(2, mongo.getCollection("treinos").countDocuments());
    }

    @Test
    void cadastroDeTreinoInvalidoDevolve400() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        assertEquals(400, post("/treinos", new TreinoRequestDTO("", "desc"), token).statusCode());
        assertEquals(400, post("/treinos", new TreinoRequestDTO("Peito/Tríceps", "desc"), token).statusCode());
    }

    @Test
    void listagemDeTreinosNaoPagina() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        for (int i = 1; i <= 30; i++) {
            post("/treinos", new TreinoRequestDTO("Treino " + i, "desc"), token);
        }

        HttpResponse<String> r = get("/treinos", token);

        assertEquals(200, r.statusCode());
        assertEquals(30, ler(r, TreinoResponseDTO[].class).length, "todos os registros de uma vez");
    }

    @Test
    void consultaDeTreinoPorNome() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Glúteo e Posterior", "desc"), token);

        HttpResponse<String> r = get("/treinos/" + segmento("Glúteo e Posterior"), token);

        assertEquals(200, r.statusCode());
        assertEquals("Glúteo e Posterior", ler(r, TreinoResponseDTO.class).nome());
        assertSemCamposTecnicos(r);
    }

    @Test
    void consultaDeTreinoInexistenteDevolve404IdentificandoOTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        HttpResponse<String> r = get("/treinos/" + segmento("Não Existe"), token);

        assertEquals(404, r.statusCode());
        assertTrue(detalhe(r).contains("Treino") && detalhe(r).contains("Não Existe"), r.body());
    }

    @Test
    void idTecnicoNaoFuncionaComoEnderecoDeTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Treino de Perna", "desc"), token);
        String idDoMongo = mongo.getCollection("treinos").find().first().getObjectId("_id").toHexString();

        assertEquals(404, get("/treinos/" + idDoMongo, token).statusCode(), "o _id nao pode enderecar o treino");
    }

    // =====================================================================================
    // Comentários
    // =====================================================================================

    @Test
    void comentariosRecebemNumeroSequencialEAutorDoToken() {
        String ana = novoUsuario("Ana", "ana@exemplo.com");
        String bruno = novoUsuario("Bruno", "bruno@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Treino de Perna", "desc"), ana);
        String url = "/treinos/" + segmento("Treino de Perna") + "/comentarios";

        HttpResponse<String> primeiro = post(url, new ComentarioRequestDTO("Muito bom"), ana);
        HttpResponse<String> segundo = post(url, new ComentarioRequestDTO("Pesado"), bruno);
        HttpResponse<String> terceiro = post(url, new ComentarioRequestDTO("Voltei e gostei"), bruno);

        assertEquals(201, primeiro.statusCode());
        assertEquals(List.of(1, 2, 3), List.of(
                ler(primeiro, ComentarioResponseDTO.class).numero(),
                ler(segundo, ComentarioResponseDTO.class).numero(),
                ler(terceiro, ComentarioResponseDTO.class).numero()));
        assertEquals("bruno@exemplo.com", ler(segundo, ComentarioResponseDTO.class).autorEmail());
        assertSemCamposTecnicos(primeiro);
    }

    @Test
    void numeroDeComentarioEhEscopadoPorTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), token);
        post("/treinos", new TreinoRequestDTO("Costas", "desc"), token);

        assertEquals(1, ler(post("/treinos/Perna/comentarios", new ComentarioRequestDTO("x"), token), ComentarioResponseDTO.class).numero());
        assertEquals(1, ler(post("/treinos/Costas/comentarios", new ComentarioRequestDTO("y"), token), ComentarioResponseDTO.class).numero());
    }

    @Test
    void comentariosFicamEmbutidosNoDocumentoDoTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Treino de Perna", "desc"), token);
        String url = "/treinos/" + segmento("Treino de Perna") + "/comentarios";
        post(url, new ComentarioRequestDTO("um"), token);
        post(url, new ComentarioRequestDTO("dois"), token);
        post(url + "/1/like", null, token);

        assertEquals(Set.of("usuarios", "treinos"), mongo.getCollectionNames(), "nenhuma colecao de comentario/avaliacao");
        Document treino = mongo.getCollection("treinos").find().first();
        List<Document> comentarios = treino.getList("comentarios", Document.class);
        assertEquals(2, comentarios.size());
        assertEquals("LIKE", comentarios.get(0).get("avaliacoes", Document.class).getString("ana@exemplo[dot]com"));
    }

    @Test
    void comentarEmTreinoInexistenteDevolve404() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        HttpResponse<String> r = post("/treinos/" + segmento("Não Existe") + "/comentarios", new ComentarioRequestDTO("x"), token);

        assertEquals(404, r.statusCode());
        assertTrue(detalhe(r).contains("Treino"), r.body());
    }

    @Test
    void comentarioInvalidoDevolve400() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), token);

        assertEquals(400, post("/treinos/Perna/comentarios", new ComentarioRequestDTO(" "), token).statusCode());
    }

    @Test
    void comentarioNaoPodeSerEditadoNemExcluido() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), token);
        post("/treinos/Perna/comentarios", new ComentarioRequestDTO("original"), token);

        for (String metodo : List.of("PUT", "PATCH", "DELETE")) {
            int status = enviar(metodo, "/treinos/Perna/comentarios/1", "{\"texto\":\"editado\"}", token).statusCode();
            assertTrue(status == 404 || status == 405, metodo + " nao pode existir, veio " + status);
            status = enviar(metodo, "/treinos/Perna/comentarios", "{\"texto\":\"editado\"}", token).statusCode();
            assertTrue(status == 404 || status == 405, metodo + " na colecao nao pode existir, veio " + status);
        }
        assertEquals("original", ler(get("/treinos/Perna/comentarios", token), ComentarioResponseDTO[].class)[0].texto());
    }

    // =====================================================================================
    // Avaliações e ranking
    // =====================================================================================

    @Test
    void likeEDislikeComTrocaDeVotoSemAcumular() {
        String ana = novoUsuario("Ana", "ana@exemplo.com");
        String bruno = novoUsuario("Bruno", "bruno@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), ana);
        post("/treinos/Perna/comentarios", new ComentarioRequestDTO("bom"), ana);
        String url = "/treinos/Perna/comentarios/1";

        ComentarioResponseDTO depoisDoLike = ler(post(url + "/like", null, bruno), ComentarioResponseDTO.class);
        assertEquals(List.of(1, 0, 1), List.of(depoisDoLike.likes(), depoisDoLike.dislikes(), depoisDoLike.score()));

        ComentarioResponseDTO depoisDaTroca = ler(post(url + "/dislike", null, bruno), ComentarioResponseDTO.class);
        assertEquals(List.of(0, 1, -1), List.of(depoisDaTroca.likes(), depoisDaTroca.dislikes(), depoisDaTroca.score()),
                "o dislike substitui o like do mesmo usuario");

        ComentarioResponseDTO voltaParaLike = ler(post(url + "/like", null, bruno), ComentarioResponseDTO.class);
        assertEquals(List.of(1, 0, 1), List.of(voltaParaLike.likes(), voltaParaLike.dislikes(), voltaParaLike.score()));

        ComentarioResponseDTO likeRepetido = ler(post(url + "/like", null, bruno), ComentarioResponseDTO.class);
        assertEquals(1, likeRepetido.likes(), "repetir o voto nao acumula");
    }

    @Test
    void avaliarNumeroInexistenteDevolve404IdentificandoOComentario() {
        String token = novoUsuario("Ana", "ana@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), token);

        HttpResponse<String> like = post("/treinos/Perna/comentarios/99/like", null, token);
        HttpResponse<String> dislike = post("/treinos/Perna/comentarios/99/dislike", null, token);

        assertEquals(404, like.statusCode());
        assertEquals(404, dislike.statusCode());
        assertTrue(detalhe(like).contains("Comentário 99") && detalhe(like).contains("Perna"), like.body());
    }

    @Test
    void avaliarEmTreinoInexistenteDevolve404IdentificandoOTreino() {
        String token = novoUsuario("Ana", "ana@exemplo.com");

        HttpResponse<String> r = post("/treinos/Nada/comentarios/1/like", null, token);

        assertEquals(404, r.statusCode());
        assertTrue(detalhe(r).startsWith("Treino"), "deve dizer que foi o treino, nao o comentario: " + r.body());
    }

    @Test
    void comentariosVemOrdenadosPorScoreENumeroNaoMuda() {
        String ana = novoUsuario("Ana", "ana@exemplo.com");
        String bruno = novoUsuario("Bruno", "bruno@exemplo.com");
        String carla = novoUsuario("Carla", "carla@exemplo.com");
        post("/treinos", new TreinoRequestDTO("Perna", "desc"), ana);
        String url = "/treinos/Perna/comentarios";
        post(url, new ComentarioRequestDTO("c1"), ana);
        post(url, new ComentarioRequestDTO("c2"), bruno);
        post(url, new ComentarioRequestDTO("c3"), carla);

        post(url + "/2/like", null, ana);
        post(url + "/2/like", null, bruno);
        post(url + "/2/like", null, carla);   // c2 = 3
        post(url + "/3/like", null, ana);
        post(url + "/3/like", null, bruno);   // c3 = 2
        post(url + "/1/dislike", null, bruno); // c1 = -1

        ComentarioResponseDTO[] antes = ler(get(url, ana), ComentarioResponseDTO[].class);
        assertEquals(List.of(2, 3, 1), numeros(antes));
        assertEquals(List.of(3, 2, -1), scores(antes));

        post(url + "/2/dislike", null, bruno);
        post(url + "/2/dislike", null, carla); // c2 = 1 - 2 = -1, empata com c1

        ComentarioResponseDTO[] depois = ler(get(url, ana), ComentarioResponseDTO[].class);
        assertEquals(List.of(3, 1, 2), numeros(depois), "reordena pelo score; no empate, menor numero primeiro");
        assertEquals(List.of(2, -1, -1), scores(depois));
        assertEquals("c2", depois[2].texto(), "o comentario 2 continua sendo o 2");
    }

    // =====================================================================================
    // helpers
    // =====================================================================================

    private String novoUsuario(String nome, String email) {
        assertEquals(201, post("/usuarios", new UsuarioRequestDTO(nome, email, SENHA), null).statusCode());
        return ler(post("/login", new LoginRequestDTO(email, SENHA), null), LoginResponseDTO.class).token();
    }

    private HttpResponse<String> get(String caminho, String token) {
        return enviar("GET", caminho, null, token);
    }

    private HttpResponse<String> post(String caminho, Object corpo, String token) {
        return enviar("POST", caminho, corpo == null ? null : json.writeValueAsString(corpo), token);
    }

    private HttpResponse<String> postCru(String caminho, String corpo, String token) {
        return enviar("POST", caminho, corpo, token);
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String corpo, String token) {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo))
                .header("Content-Type", "application/json");
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        try {
            return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T ler(HttpResponse<String> resposta, Class<T> tipo) {
        return json.readValue(resposta.body(), tipo);
    }

    private String detalhe(HttpResponse<String> resposta) {
        Object detail = ler(resposta, Map.class).get("detail");
        assertNotNull(detail, "resposta de erro sem 'detail': " + resposta.body());
        return detail.toString();
    }

    private static String segmento(String nome) {
        return UriUtils.encodePathSegment(nome, StandardCharsets.UTF_8);
    }

    private static void assertSemCamposTecnicos(HttpResponse<String> resposta) {
        for (String campo : List.of("\"id\"", "\"_id\"", "\"versao\"", "\"senha\"", "\"_class\"")) {
            assertFalse(resposta.body().contains(campo), "resposta expoe " + campo + ": " + resposta.body());
        }
    }

    private static List<Integer> numeros(ComentarioResponseDTO[] lista) {
        return Arrays.stream(lista).map(ComentarioResponseDTO::numero).toList();
    }

    private static List<Integer> scores(ComentarioResponseDTO[] lista) {
        return Arrays.stream(lista).map(ComentarioResponseDTO::score).toList();
    }
}
