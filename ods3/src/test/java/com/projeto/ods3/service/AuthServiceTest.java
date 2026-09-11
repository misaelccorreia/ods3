package com.projeto.ods3.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import com.projeto.ods3.dto.auth.LoginRequestDTO;
import com.projeto.ods3.exception.CredenciaisInvalidasException;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.UsuarioRepository;
import com.projeto.ods3.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    final JwtUtil jwtUtil = new JwtUtil(
            Base64.getEncoder().encodeToString("chave-de-teste-com-32-bytes-ok!!".getBytes(StandardCharsets.UTF_8)), 60_000);

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository, passwordEncoder, jwtUtil);
    }

    private Usuario ana() {
        return new Usuario("Ana", "ana@exemplo.com", passwordEncoder.encode("segredo123"));
    }

    @Test
    void credenciaisValidasDevolvemTokenComSubIgualAoEmail() {
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana()));

        String token = service.login(new LoginRequestDTO("ana@exemplo.com", "segredo123")).token();

        assertTrue(jwtUtil.validarToken(token));
        assertEquals("ana@exemplo.com", jwtUtil.extrairEmail(token));
    }

    @Test
    void senhaErradaNaoDevolveToken() {
        when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana()));

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequestDTO("ana@exemplo.com", "errada")));
    }

    @Test
    void emailInexistenteNaoDevolveToken() {
        when(usuarioRepository.findByEmail("ninguem@exemplo.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.login(new LoginRequestDTO("ninguem@exemplo.com", "segredo123")));
    }
}
