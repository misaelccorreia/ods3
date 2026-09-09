package com.projeto.ods3.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Usuário da plataforma — coleção própria no MongoDB.
 *
 * <p>É endereçado por {@code email} em toda a aplicação: no login, no claim {@code sub}
 * do JWT e como autor denormalizado de treinos e comentários. O {@code id} existe apenas
 * como detalhe interno de persistência e não é lido nem atribuído por nenhuma camada.
 */
@Document(collection = "usuarios")
@Getter
@NoArgsConstructor
public class Usuario {

    /** Gerado pelo MongoDB no primeiro save. Sem setter — a aplicação nunca o atribui. */
    @Id
    private String id;

    @Setter
    private String nome;

    @Setter
    @Indexed(unique = true)
    private String email;

    /** Hash da senha. Quem aplica o hash é o UsuarioService, não a entidade. */
    @Setter
    private String senha;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }
}
