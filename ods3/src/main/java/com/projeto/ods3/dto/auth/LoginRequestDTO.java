package com.projeto.ods3.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String senha) {

    @Override
    public String toString() {
        return "LoginRequestDTO[email=" + email + ", senha=***]";
    }
}
