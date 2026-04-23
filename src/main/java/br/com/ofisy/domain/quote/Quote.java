package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quotes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote {

    public static final String ACTION_APPROVE = "aprovar";
    public static final String ACTION_REPROVE = "reprovar";
    public static final String ACTION_REPROVE_PARTIALLY = "reprovar parcialmente";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteStockItem> stockItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public static Quote create(UUID serviceOrderId, List<QuoteStockItem> stockItems) {
        Quote quote = new Quote();
        quote.serviceOrderId = serviceOrderId;
        quote.status = QuoteStatus.PENDING;
        quote.stockItems = stockItems;
        quote.totalPrice = calculateTotal(stockItems);
        quote.createdAt = LocalDateTime.now();
        stockItems.forEach(item -> item.setQuote(quote));
        return quote;
    }

    public void approve() {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_APPROVE, this.status);
        }
        this.status = QuoteStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reprove() {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_REPROVE, this.status);
        }
        this.status = QuoteStatus.REPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reprovePartially() {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_REPROVE_PARTIALLY, this.status);
        }
        this.status = QuoteStatus.PARTIALLY_REPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    private static BigDecimal calculateTotal(List<QuoteStockItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
