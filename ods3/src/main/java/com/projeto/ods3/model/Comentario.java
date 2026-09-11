package com.projeto.ods3.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Experiência de um usuário sobre um treino.
 *
 * <p>Não é uma coleção própria: é um POJO serializado como sub-documento dentro da lista
 * {@code comentarios} de {@link Treino}. Por isso não tem {@code @Document} nem {@code @Id} —
 * é endereçado pelo {@code numero}, sequencial dentro do treino a que pertence.
 *
 * <p>Depois de criado, só o mapa de avaliações muda; texto e número são imutáveis (Seção 6.1).
 */
@Getter
@NoArgsConstructor
public class Comentario {

    /** Sequencial dentro do treino, atribuído pelo ComentarioService na criação. Imutável. */
    private int numero;

    /** Imutável: a spec proíbe editar ou excluir o texto de um comentário. */
    private String texto;

    private String autorNome;

    private String autorEmail;

    private LocalDateTime dataCriacao;

    /** Chave: e-mail de quem votou. O próprio Map garante um voto por usuário. */
    private Map<String, TipoAvaliacao> avaliacoes = new HashMap<>();

    public Comentario(int numero, String texto, String autorNome, String autorEmail) {
        this.numero = numero;
        this.texto = texto;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.dataCriacao = LocalDateTime.now();
    }

    /**
     * Registra o voto de um usuário. Se ele já havia votado neste comentário, o voto
     * anterior é substituído — o {@code put} do Map já é o upsert que a regra pede.
     */
    public void registrarAvaliacao(String email, TipoAvaliacao tipo) {
        avaliacoes.put(email, tipo);
    }

    /** Score de exibição: quantidade de LIKE menos quantidade de DISLIKE. Dado derivado. */
    public int calcularScore() {
        long likes = avaliacoes.values().stream().filter(t -> t == TipoAvaliacao.LIKE).count();
        long dislikes = avaliacoes.values().stream().filter(t -> t == TipoAvaliacao.DISLIKE).count();
        return (int) (likes - dislikes);
    }

    /** Somente leitura: votar é responsabilidade de {@link #registrarAvaliacao}. */
    public Map<String, TipoAvaliacao> getAvaliacoes() {
        return Collections.unmodifiableMap(avaliacoes);
    }
}
