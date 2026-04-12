package br.com.ofisy.application.stock.exceptions;

import java.util.UUID;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(UUID stockId) {
        super("Estoque não encontrado: ID" + stockId);
    }
}
