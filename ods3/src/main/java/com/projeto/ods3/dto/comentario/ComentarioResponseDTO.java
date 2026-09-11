package com.projeto.ods3.dto.comentario;

import java.time.LocalDateTime;

import com.projeto.ods3.model.Comentario;
import com.projeto.ods3.model.TipoAvaliacao;

/** {@code numero} é o que o cliente usa para montar a URL de like/dislike. */
public record ComentarioResponseDTO(
        int numero,
        String texto,
        String autorNome,
        String autorEmail,
        LocalDateTime dataCriacao,
        int likes,
        int dislikes,
        int score) {

    public static ComentarioResponseDTO de(Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getNumero(),
                comentario.getTexto(),
                comentario.getAutorNome(),
                comentario.getAutorEmail(),
                comentario.getDataCriacao(),
                contar(comentario, TipoAvaliacao.LIKE),
                contar(comentario, TipoAvaliacao.DISLIKE),
                comentario.calcularScore());
    }

    private static int contar(Comentario comentario, TipoAvaliacao tipo) {
        return (int) comentario.getAvaliacoes().values().stream().filter(t -> t == tipo).count();
    }
}
