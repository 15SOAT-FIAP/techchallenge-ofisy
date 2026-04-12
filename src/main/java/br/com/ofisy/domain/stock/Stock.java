package br.com.ofisy.domain.stock;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String productName;

    @Column(nullable = false)
    private String description;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private String category;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private Integer minThreshold;

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

    public static Stock create(String productName, String description, Integer quantity, BigDecimal unitPrice, String category, Integer minThreshold) {
        return new Stock(productName, description, quantity, unitPrice, category, minThreshold);
    }

    public void addQuantity(Integer quantity) {
        this.quantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void consumeQuantity(Integer quantity) {
        this.quantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return minThreshold != null && quantity <= minThreshold;
    }
}
