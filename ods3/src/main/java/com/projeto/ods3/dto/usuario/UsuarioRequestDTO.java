package com.projeto.ods3.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequestDTO(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String senha) {

    /** Não deixa a senha vazar em log: o toString padrão de record imprimiria todos os campos. */
    @Override
    public String toString() {
        return "UsuarioRequestDTO[nome=" + nome + ", email=" + email + ", senha=***]";
    }
}
