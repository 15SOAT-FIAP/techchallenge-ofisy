package br.com.ofisy.domain.customer.exceptions;

import java.util.UUID;

public class CustomerAlreadyActiveException extends RuntimeException {
    public CustomerAlreadyActiveException(UUID id) {
        super("Cliente " + id + " já está ativo.");
    }
}