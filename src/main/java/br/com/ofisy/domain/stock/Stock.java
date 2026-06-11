package br.com.ofisy.domain.stock;

import br.com.ofisy.domain.stock.exceptions.InvalidOperationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock {

    private UUID id;

    private String productName;

    private String description;

    private Integer quantity = 0;

    private BigDecimal unitPrice;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer minThreshold = 0;

    private Stock(String productName, String description, Integer quantity, BigDecimal unitPrice, String category, Integer minThreshold) {
        this.productName = productName;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.category = category;
        this.minThreshold = minThreshold;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Stock reconstruct(UUID id, String productName, String description,
                                    Integer quantity, BigDecimal unitPrice, String category,
                                    Integer minThreshold, LocalDateTime createdAt, LocalDateTime updatedAt) {

        Stock stock = new Stock(productName, description, quantity, unitPrice, category, minThreshold);

        stock.id = id;
        stock.createdAt = createdAt;
        stock.updatedAt = updatedAt;

        return stock;
    }

    public static Stock create(String productName, String description, Integer quantity, BigDecimal unitPrice, String category, Integer minThreshold) {
        return new Stock(productName, description, quantity, unitPrice, category, minThreshold);
    }

    public void update(String productName, String description, BigDecimal unitPrice, String category, Integer minThreshold) {
        if (productName != null) {
            this.productName = productName;
        }

        if (description != null) {
            this.description = description;
        }

        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.unitPrice = unitPrice;
        }

        if (category != null) {
            this.category = category;
        }

        if (minThreshold != null && minThreshold >= 0) {
            this.minThreshold = minThreshold;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void addQuantity(Integer quantity) {
        validateQuantity(quantity);

        this.quantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void consumeQuantity(Integer quantity) {
        validateQuantity(quantity);

        this.quantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return minThreshold != null && quantity <= minThreshold;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidOperationException(
                    "Quantidade informada é inválida: " + quantity + ". Deve ser maior que zero."
            );
        }
    }
}
