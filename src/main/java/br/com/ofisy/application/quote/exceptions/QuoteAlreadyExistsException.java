package br.com.ofisy.application.quote.exceptions;

import java.util.UUID;

public class QuoteAlreadyExistsException extends RuntimeException {
    public QuoteAlreadyExistsException(UUID serviceOrderId) {
        super("Já existe um orçamento para a ordem de serviço " + serviceOrderId);
    }
}
