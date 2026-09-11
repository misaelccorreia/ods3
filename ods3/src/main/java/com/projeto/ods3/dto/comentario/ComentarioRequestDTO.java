package com.projeto.ods3.dto.comentario;

import jakarta.validation.constraints.NotBlank;

/** Só o texto: o treino vem da URL e o autor, do token. */
public record ComentarioRequestDTO(@NotBlank String texto) {
}
