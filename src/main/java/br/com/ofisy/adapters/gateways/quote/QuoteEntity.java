package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quotes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID serviceOrderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuoteStockItemEntity> stockItems = new ArrayList<>();

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuoteServiceItemEntity> serviceItems = new ArrayList<>();

    private String quoteRefusalReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void update(QuoteStatus status, BigDecimal totalPrice,
                       String quoteRefusalReason, LocalDateTime updatedAt) {
        this.status = status;
        this.totalPrice = totalPrice;
        this.quoteRefusalReason = quoteRefusalReason;
        this.updatedAt = updatedAt;
    }
}