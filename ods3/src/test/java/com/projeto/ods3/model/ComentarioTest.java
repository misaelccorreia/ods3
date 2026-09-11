package com.projeto.ods3.model;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.junit.jupiter.api.Assertions.*;

class ComentarioTest {

    private Comentario novoComentario() {
        return new Comentario(1, "Treino puxado, mas vale a pena", "Misael", "misael@exemplo.com");
    }

    @Nested
    @DisplayName("registrarAvaliacao")
    class RegistrarAvaliacao {

        @Test
        @DisplayName("cria a avaliação quando o usuário ainda não votou")
        void criaAvaliacaoNova() {
            Comentario comentario = novoComentario();

            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);

            assertEquals(1, comentario.getAvaliacoes().size());
            assertEquals(TipoAvaliacao.LIKE, comentario.getAvaliacoes().get("ana@exemplo.com"));
        }

        @Test
        @DisplayName("substitui o voto anterior quando o mesmo usuário troca de like para dislike")
        void substituiLikePorDislike() {
            Comentario comentario = novoComentario();

            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.DISLIKE);

            assertEquals(1, comentario.getAvaliacoes().size(), "nao pode acumular votos do mesmo usuario");
            assertEquals(TipoAvaliacao.DISLIKE, comentario.getAvaliacoes().get("ana@exemplo.com"));
            assertEquals(-1, comentario.calcularScore());
        }

        @Test
        @DisplayName("substitui o voto anterior quando o mesmo usuário troca de dislike para like")
        void substituiDislikePorLike() {
            Comentario comentario = novoComentario();

            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.DISLIKE);
            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);

            assertEquals(1, comentario.getAvaliacoes().size());
            assertEquals(TipoAvaliacao.LIKE, comentario.getAvaliacoes().get("ana@exemplo.com"));
            assertEquals(1, comentario.calcularScore());
        }

        @Test
        @DisplayName("repetir o mesmo voto não acumula nem muda o score")
        void repetirMesmoVotoNaoAcumula() {
            Comentario comentario = novoComentario();

            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);

            assertEquals(1, comentario.getAvaliacoes().size());
            assertEquals(1, comentario.calcularScore());
        }

        @Test
        @DisplayName("votos de usuários diferentes coexistem")
        void usuariosDiferentesCoexistem() {
            Comentario comentario = novoComentario();

            comentario.registrarAvaliacao("ana@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("bruno@exemplo.com", TipoAvaliacao.DISLIKE);

            assertEquals(2, comentario.getAvaliacoes().size());
        }
    }

    @Nested
    @DisplayName("calcularScore")
    class CalcularScore {

        @Test
        @DisplayName("é zero quando ninguém avaliou")
        void semAvaliacoes() {
            assertEquals(0, novoComentario().calcularScore());
        }

        @Test
        @DisplayName("é likes menos dislikes quando há mais likes")
        void scorePositivo() {
            Comentario comentario = novoComentario();
            comentario.registrarAvaliacao("a@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("b@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("c@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("d@exemplo.com", TipoAvaliacao.DISLIKE);

            assertEquals(2, comentario.calcularScore());
        }

        @Test
        @DisplayName("é zero no empate entre likes e dislikes")
        void scoreEmpatado() {
            Comentario comentario = novoComentario();
            comentario.registrarAvaliacao("a@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("b@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("c@exemplo.com", TipoAvaliacao.DISLIKE);
            comentario.registrarAvaliacao("d@exemplo.com", TipoAvaliacao.DISLIKE);

            assertEquals(0, comentario.calcularScore());
        }

        @Test
        @DisplayName("é negativo quando há mais dislikes")
        void scoreNegativo() {
            Comentario comentario = novoComentario();
            comentario.registrarAvaliacao("a@exemplo.com", TipoAvaliacao.LIKE);
            comentario.registrarAvaliacao("b@exemplo.com", TipoAvaliacao.DISLIKE);
            comentario.registrarAvaliacao("c@exemplo.com", TipoAvaliacao.DISLIKE);
            comentario.registrarAvaliacao("d@exemplo.com", TipoAvaliacao.DISLIKE);

            assertEquals(-2, comentario.calcularScore());
        }
    }

    @Nested
    @DisplayName("imutabilidade e modelagem (Seções 6 e 6.1)")
    class Imutabilidade {

        @Test
        @DisplayName("não existe método público que altere texto ou numero")
        void semSetterDeTextoOuNumero() {
            var proibidos = Arrays.stream(Comentario.class.getMethods())
                    .map(Method::getName)
                    .filter(n -> n.equalsIgnoreCase("setTexto") || n.equalsIgnoreCase("setNumero"))
                    .toList();

            assertTrue(proibidos.isEmpty(), "metodos que quebram a imutabilidade: " + proibidos);
        }

        @Test
        @DisplayName("o mapa devolvido por getAvaliacoes é somente leitura")
        void mapaDeAvaliacoesNaoEditavelPorFora() {
            Comentario comentario = novoComentario();

            assertThrows(UnsupportedOperationException.class,
                    () -> comentario.getAvaliacoes().put("intruso@exemplo.com", TipoAvaliacao.LIKE));
        }

        @Test
        @DisplayName("não é uma coleção própria: sem @Document e sem @Id")
        void naoEhDocumentoProprio() {
            assertFalse(Comentario.class.isAnnotationPresent(Document.class),
                    "Comentario nao pode ser @Document — e embutido em Treino");

            boolean temId = Arrays.stream(Comentario.class.getDeclaredFields())
                    .anyMatch(f -> f.isAnnotationPresent(Id.class));
            assertFalse(temId, "Comentario nao pode ter campo @Id");
        }
    }

    @Nested
    @DisplayName("TipoAvaliacao")
    class ValoresDoEnum {

        @Test
        @DisplayName("possui exatamente LIKE e DISLIKE")
        void doisValores() {
            assertArrayEquals(new TipoAvaliacao[]{TipoAvaliacao.LIKE, TipoAvaliacao.DISLIKE},
                    TipoAvaliacao.values());
        }
    }
}
