package com.projeto.ods3.dto.treino;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Sem autor: ele vem do token. */
public record TreinoRequestDTO(
        // o nome vira segmento de URL em /treinos/{nome}; esses caracteres o tornariam inalcançável
        @NotBlank
        @Pattern(regexp = "[^/\\\\%;]+", message = "não pode conter os caracteres / \\ % ;")
        String nome,
        @NotBlank String descricao) {
}
