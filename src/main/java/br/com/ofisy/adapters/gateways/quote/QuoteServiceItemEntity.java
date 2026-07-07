package br.com.ofisy.adapters.gateways.quote;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_service_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quote_id", "service_order_executions_id"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteServiceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private QuoteEntity quote;

    @Column(name = "service_order_executions_id", nullable = false)
    private UUID serviceOrderExecutionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    void update(UUID serviceOrderExecutionId, BigDecimal price, LocalDateTime updatedAt) {
        this.serviceOrderExecutionId = serviceOrderExecutionId;
        this.price = price;
        this.updatedAt = updatedAt;
    }
}
