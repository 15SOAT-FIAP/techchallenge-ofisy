package br.com.ofisy.application.notification.ports.in;

import java.util.Objects;
import java.util.UUID;

public record CreateLowStockCommand(
    UUID stockId,
    String productName,
    int currentQuantity,
    int minThreshold
) {
    public CreateLowStockCommand {
        if (stockId == null) throw new IllegalArgumentException("stockId não pode ser nulo");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("productName inválido");
        if (currentQuantity < 0) throw new IllegalArgumentException("currentQuantity inválido");
        if (minThreshold < 0) throw new IllegalArgumentException("minThreshold inválido");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateLowStockCommand that = (CreateLowStockCommand) o;
        return currentQuantity == that.currentQuantity && 
               minThreshold == that.minThreshold && 
               Objects.equals(stockId, that.stockId) && 
               Objects.equals(productName, that.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockId, productName, currentQuantity, minThreshold);
    }
}
