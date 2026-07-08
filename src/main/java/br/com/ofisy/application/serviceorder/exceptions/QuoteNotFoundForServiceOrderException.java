package br.com.ofisy.application.serviceorder.exceptions;

import java.util.UUID;

public class QuoteNotFoundForServiceOrderException extends RuntimeException {
    public QuoteNotFoundForServiceOrderException(UUID id) {
        super("Orçamento não encontrado para OS com id: " + id);
    }
}
