/**
 * DTOs de comentário. O request carrega apenas o texto; autor e treino vêm do token
 * e da URL. O response expõe {@code numero}, além de likes, dislikes e score derivados
 * das avaliações. Não há DTO de avaliação — os endpoints de like/dislike não têm corpo.
 */
package com.projeto.ods3.dto.comentario;
