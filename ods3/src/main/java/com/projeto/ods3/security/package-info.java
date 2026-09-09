/**
 * Autenticação e autorização — Spring Security com JWT.
 *
 * <p>{@code SecurityConfig} define o que é público ({@code POST /usuarios} e
 * {@code POST /login}) e o que exige token — todo o resto, inclusive os {@code GET}s.
 * {@code JwtUtil} emite e valida o token; {@code JwtFilter} extrai o claim {@code sub},
 * que é o e-mail do usuário autenticado, nunca o id técnico.
 */
package com.projeto.ods3.security;
