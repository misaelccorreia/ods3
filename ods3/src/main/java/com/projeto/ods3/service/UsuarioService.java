package com.projeto.ods3.service;

import com.projeto.ods3.dto.usuario.UsuarioRequestDTO;
import com.projeto.ods3.dto.usuario.UsuarioResponseDTO;
import com.projeto.ods3.exception.EmailJaCadastradoException;
import com.projeto.ods3.model.Usuario;
import com.projeto.ods3.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dados) {
        if (usuarioRepository.existsByEmail(dados.email())) {
            throw new EmailJaCadastradoException(dados.email());
        }

        Usuario usuario = new Usuario(dados.nome(), dados.email(), passwordEncoder.encode(dados.senha()));
        try {
            return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
        } catch (DuplicateKeyException e) {
            // outro cadastro com o mesmo e-mail entrou entre o existsByEmail e o save
            throw new EmailJaCadastradoException(dados.email());
        }
    }
}
