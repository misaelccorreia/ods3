package com.projeto.ods3.dto.treino;

import java.time.LocalDateTime;

import com.projeto.ods3.model.Treino;

public record TreinoResponseDTO(
        String nome,
        String descricao,
        String autorNome,
        String autorEmail,
        LocalDateTime dataCriacao) {

    public static TreinoResponseDTO de(Treino treino) {
        return new TreinoResponseDTO(treino.getNome(), treino.getDescricao(),
                treino.getAutorNome(), treino.getAutorEmail(), treino.getDataCriacao());
    }
}
