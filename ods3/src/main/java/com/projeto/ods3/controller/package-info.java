/**
 * Camada de exposição da API REST. Recebe a requisição, valida o DTO de entrada,
 * delega ao Service e devolve a resposta HTTP — sem implementar regra de negócio.
 *
 * <p>Os recursos são endereçados por campo de negócio ({@code nome} do treino,
 * {@code numero} do comentário), nunca por id técnico do MongoDB. O e-mail do autor
 * de cada ação vem do token JWT, não do corpo da requisição.
 */
package com.projeto.ods3.controller;
