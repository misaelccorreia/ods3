package com.projeto.ods3.exception;

/** Mesma mensagem para e-mail inexistente e senha errada, para não revelar quais e-mails existem. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
