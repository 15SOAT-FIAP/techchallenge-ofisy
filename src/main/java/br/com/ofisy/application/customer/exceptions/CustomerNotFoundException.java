package br.com.ofisy.application.customer.exceptions;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("Cliente não encontrado com o ID: " + id);
    }
}
