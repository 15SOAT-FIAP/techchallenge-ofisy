package br.com.ofisy.application.stock.exceptions;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID stockId) {
        super("Estoque insuficiente: ID " + stockId);
    }
}
