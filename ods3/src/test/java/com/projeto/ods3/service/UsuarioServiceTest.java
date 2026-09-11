package com.projeto.ods3.service;

import com.projeto.ods3.dto.usuario.UsuarioRequestDTO;
import com.projeto.ods3.dto.usuario.UsuarioResponseDTO;
import com.projeto.ods3.exception.EmailJaCadastradoException;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    // encoder real: o teste precisa provar que o que vai para o banco é um hash de verdade
    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void cadastraComSenhaEmHash() {
        when(usuarioRepository.existsByEmail("ana@exemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO resposta = service.cadastrar(new UsuarioRequestDTO("Ana", "ana@exemplo.com", "segredo123"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertNotEquals("segredo123", salvo.getValue().getSenha(), "senha nao pode ser gravada em texto plano");
        assertTrue(passwordEncoder.matches("segredo123", salvo.getValue().getSenha()));
        assertEquals("Ana", resposta.nome());
        assertEquals("ana@exemplo.com", resposta.email());
    }

    @Test
    void emailJaCadastradoFalhaSemSalvar() {
        when(usuarioRepository.existsByEmail("ana@exemplo.com")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class,
                () -> service.cadastrar(new UsuarioRequestDTO("Ana", "ana@exemplo.com", "segredo123")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastroSimultaneoBarradoPeloIndiceUnicoViraEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("ana@exemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new DuplicateKeyException("E11000"));

        assertThrows(EmailJaCadastradoException.class,
                () -> service.cadastrar(new UsuarioRequestDTO("Ana", "ana@exemplo.com", "segredo123")));
    }
}
