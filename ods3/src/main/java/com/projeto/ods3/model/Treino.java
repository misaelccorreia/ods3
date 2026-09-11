package com.projeto.ods3.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Treino compartilhado na plataforma — entidade central do sistema e agregado raiz.
 *
 * <p>É endereçado por {@code nome}, único globalmente e comparado de forma exata e
 * case-sensitive. O autor não é uma referência para {@code Usuario}: são apenas
 * {@code autorNome} e {@code autorEmail}, copiados do usuário autenticado na criação,
 * para não duplicar o hash de senha em cada treino.
 */
@Document(collection = "treinos")
@Getter
@NoArgsConstructor
public class    Treino {

    /** Gerado pelo MongoDB no primeiro save. Sem setter — a aplicação nunca o atribui. */
    @Id
    private String id;

    @Setter
    @Indexed(unique = true)
    private String nome;

    @Setter
    private String descricao;

    /** Recorte denormalizado do autor, gravado na criação pelo TreinoService. */
    @Setter
    private String autorNome;

    @Setter
    private String autorEmail;

    /** Auditoria. Sem setter: definido na criação e nunca alterado depois. */
    private LocalDateTime dataCriacao;

    /**
     * Comentários embutidos — array de sub-documentos dentro do próprio Treino,
     * não uma coleção separada. É esta lista que o ComentarioService manipula.
     */
    private List<Comentario> comentarios = new ArrayList<>();

    public Treino(String nome, String descricao, String autorNome, String autorEmail) {
        this.nome = nome;
        this.descricao = descricao;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.dataCriacao = LocalDateTime.now();
    }
}
