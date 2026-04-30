package br.com.ofisy.application.quote.exceptions;

public class QuoteItemAlreadyExistsException extends RuntimeException {
    public QuoteItemAlreadyExistsException(String itemName) {
        super("Item '" + itemName + "' já existe neste orçamento");
    }
}