package br.com.ofisy.domain.user.exceptions;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String email) {
        super("Usuário inativo: " + email);
    }
}
