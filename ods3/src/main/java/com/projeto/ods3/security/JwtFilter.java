package com.projeto.ods3.security;

import java.io.IOException;
import java.util.List;

import com.projeto.ods3.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica a requisição a partir do header {@code Authorization: Bearer <token>}.
 * O principal fica sendo o e-mail do usuário; token ausente ou inválido simplesmente não
 * autentica, e a SecurityConfig barra a rota protegida com 401 antes do Controller.
 *
 * <p>Não é um {@code @Component} de propósito: se fosse, o Boot o registraria também como
 * filtro de servlet comum, fora da cadeia de segurança.
 */
public class JwtFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(PREFIXO)) {
            String token = header.substring(PREFIXO.length());
            if (jwtUtil.validarToken(token)) {
                String email = jwtUtil.extrairEmail(token);
                // token valido de um usuario que nao existe mais nao autentica
                if (usuarioRepository.existsByEmail(email)) {
                    SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(email, null, List.of()));
                }
            }
        }

        chain.doFilter(request, response);
    }
}
