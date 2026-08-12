package br.com.ofisy.domain.customer.exceptions;

import java.util.UUID;

public class CustomerAlreadyInactiveException extends RuntimeException {
    public CustomerAlreadyInactiveException(UUID id) {
        super("Cliente " + id + " já está desativado.");
    }
}