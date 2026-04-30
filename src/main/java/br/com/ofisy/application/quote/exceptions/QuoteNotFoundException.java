package br.com.ofisy.application.quote.exceptions;

import java.util.UUID;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(UUID id) {
        super("Orçamento com id " + id + " não encontrado!");
    }
}
