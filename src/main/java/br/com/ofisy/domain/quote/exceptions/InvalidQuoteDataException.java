package br.com.ofisy.domain.quote.exceptions;

import java.util.UUID;

public class InvalidQuoteDataException extends RuntimeException {
    public InvalidQuoteDataException(UUID serviceOrderId) {
        super("Não foi possível criar um orçamento para a OS " + serviceOrderId + ". É obrigatório uma peça ou um serviço válido no orçamento!");
    }
}
