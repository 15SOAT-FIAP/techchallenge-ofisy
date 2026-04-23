package br.com.ofisy.domain.quote.exceptions;

import br.com.ofisy.domain.quote.QuoteStatus;

public class InvalidQuoteStatusException extends RuntimeException {
    public InvalidQuoteStatusException(String operation, QuoteStatus currentStatus) {
        super("Não é possível " + operation + " um orçamento com status " + currentStatus);
    }
}
