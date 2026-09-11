package com.projeto.ods3.exception;

public class ComentarioNaoEncontradoException extends RuntimeException {

    public ComentarioNaoEncontradoException(String nomeTreino, int numero) {
        super("Comentário " + numero + " não encontrado no treino '" + nomeTreino + "'");
    }
}
