package br.com.ofisy.domain.customer.exceptions;

import java.util.UUID;

public class InactiveCustomerException extends RuntimeException {
    public InactiveCustomerException(UUID id) {
        super("Cliente " + id + " está inativo.");
    }
}