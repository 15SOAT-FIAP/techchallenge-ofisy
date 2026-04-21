package br.com.ofisy.domain.user.exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String errorMessage) {
        super(errorMessage);
    }
}
