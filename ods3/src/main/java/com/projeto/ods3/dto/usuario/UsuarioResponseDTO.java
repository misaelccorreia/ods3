package com.projeto.ods3.dto.usuario;

import com.projeto.ods3.model.Usuario;

/** Sem senha e sem id técnico — o usuário é identificado pelo e-mail. */
public record UsuarioResponseDTO(String nome, String email) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getNome(), usuario.getEmail());
    }
}
