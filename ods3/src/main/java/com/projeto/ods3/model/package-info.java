/**
 * Domínio da aplicação.
 *
 * <p>{@code Usuario} e {@code Treino} são coleções próprias ({@code @Document});
 * {@code Comentario} é estrutura embutida na lista {@code comentarios} de {@code Treino},
 * e as avaliações são um {@code Map<email, TipoAvaliacao>} dentro do comentário.
 * O id gerado pelo MongoDB nunca é lido, gravado ou exposto pela aplicação.
 */
package com.projeto.ods3.model;
