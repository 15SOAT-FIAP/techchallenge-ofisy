package br.com.ofisy.application.user.exceptions;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super("Usuário com email " + email + " não encontrado!");
    }
}

