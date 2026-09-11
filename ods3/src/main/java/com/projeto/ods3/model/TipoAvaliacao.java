package com.projeto.ods3.model;

/**
 * Os dois únicos tipos de avaliação que um comentário admite.
 *
 * <p>Não leva anotação de persistência: como valor do {@code Map<String, TipoAvaliacao>}
 * de {@link Comentario}, o Spring Data MongoDB o serializa como string automaticamente.
 */
public enum TipoAvaliacao {
    LIKE,
    DISLIKE
}
