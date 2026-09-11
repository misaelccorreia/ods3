package com.projeto.ods3.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SEGREDO = base64("chave-de-teste-com-32-bytes-ok!!");
    private static final String OUTRO_SEGREDO = base64("outra-chave-de-teste-32-bytes!!!");

    private final JwtUtil jwtUtil = new JwtUtil(SEGREDO, 60_000);

    @Test
    void tokenGeradoEhValido() {
        assertTrue(jwtUtil.validarToken(jwtUtil.gerarToken("ana@exemplo.com")));
    }

    @Test
    void extrairEmailDevolveOEmailUsadoNaGeracao() {
        assertEquals("ana@exemplo.com", jwtUtil.extrairEmail(jwtUtil.gerarToken("ana@exemplo.com")));
    }

    @Test
    void claimSubEhOEmail() {
        String payload = decodificar(jwtUtil.gerarToken("ana@exemplo.com").split("\\.")[1]);
        assertTrue(payload.contains("\"sub\":\"ana@exemplo.com\""), payload);
    }

    @Test
    void tokenAdulteradoFalha() {
        String[] partes = jwtUtil.gerarToken("ana@exemplo.com").split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"intruso@exemplo.com\"}".getBytes(StandardCharsets.UTF_8));

        assertFalse(jwtUtil.validarToken(partes[0] + "." + payloadFalso + "." + partes[2]));
    }

    @Test
    void tokenExpiradoFalha() {
        JwtUtil expiraNaHora = new JwtUtil(SEGREDO, -1_000);
        assertFalse(expiraNaHora.validarToken(expiraNaHora.gerarToken("ana@exemplo.com")));
    }

    @Test
    void tokenAssinadoComOutraChaveFalha() {
        String deOutraChave = new JwtUtil(OUTRO_SEGREDO, 60_000).gerarToken("ana@exemplo.com");
        assertFalse(jwtUtil.validarToken(deOutraChave));
    }

    @Test
    void tokenMalformadoOuNuloFalha() {
        assertFalse(jwtUtil.validarToken("nao-eh-um-jwt"));
        assertFalse(jwtUtil.validarToken(""));
        assertFalse(jwtUtil.validarToken(null));
    }

    private static String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodificar(String base64Url) {
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }
}
