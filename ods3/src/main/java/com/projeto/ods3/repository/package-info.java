/**
 * Camada de persistência, sobre as abstrações do Spring Data MongoDB.
 * Responsabilidade exclusiva de comunicação com o banco — sem regra de negócio.
 *
 * <p>Duas coleções, dois repositórios: {@code UsuarioRepository} e {@code TreinoRepository}.
 * Não existe repositório de comentário nem de avaliação — ambos são estruturas embutidas
 * no documento {@code Treino}. A busca externa é sempre por campo de negócio
 * ({@code findByNome}, {@code findByEmail}), nunca por {@code findById}.
 */
package com.projeto.ods3.repository;
