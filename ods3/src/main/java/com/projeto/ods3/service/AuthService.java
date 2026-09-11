package com.projeto.ods3.service;

import com.projeto.ods3.dto.auth.LoginRequestDTO;
import com.projeto.ods3.dto.auth.LoginResponseDTO;
import com.projeto.ods3.exception.CredenciaisInvalidasException;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.UsuarioRepository;
import com.projeto.ods3.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.email())
                .filter(u -> passwordEncoder.matches(dados.senha(), u.getSenha()))
                .orElseThrow(CredenciaisInvalidasException::new);

        return new LoginResponseDTO(jwtUtil.gerarToken(usuario.getEmail()));
    }
}
