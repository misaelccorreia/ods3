package com.projeto.ods3.exception;

public class TreinoNaoEncontradoException extends RuntimeException {

    public TreinoNaoEncontradoException(String nome) {
        super("Treino não encontrado: '" + nome + "'");
    }
}
