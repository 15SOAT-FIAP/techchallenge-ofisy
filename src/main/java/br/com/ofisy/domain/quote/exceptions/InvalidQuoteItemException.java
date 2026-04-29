package br.com.ofisy.domain.quote.exceptions;

public class InvalidQuoteItemException extends RuntimeException {
    public InvalidQuoteItemException(String message) {
        super(message);
    }
}
