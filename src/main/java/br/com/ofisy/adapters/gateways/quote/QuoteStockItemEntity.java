package br.com.ofisy.adapters.gateways.quote;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_stock_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quote_id", "stock_id"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteStockItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private QuoteEntity quote;

    @Column(name = "stock_id", nullable = false)
    private UUID stockId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    void update(UUID stockId, BigDecimal unitPrice, Integer quantity, LocalDateTime updatedAt) {
        this.stockId = stockId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }
}
