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

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteServiceItem> serviceItems = new ArrayList<>();

    @Column
    private String quoteRefusalReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public static Quote create(UUID serviceOrderId,
                               List<QuoteStockItem> stockItems,
                               List<QuoteServiceItem> serviceItems) {
        Quote quote = new Quote();
        quote.serviceOrderId = serviceOrderId;
        quote.status = QuoteStatus.PENDING;
        quote.stockItems = stockItems != null ? stockItems : new ArrayList<>();
        quote.serviceItems = serviceItems != null ? serviceItems : new ArrayList<>();
        //quote.totalPrice = calculateTotal(quote.stockItems, quote.serviceItems); - aguardando implementação total serviços
        quote.totalPrice = calculateTotal(quote.stockItems);
        quote.createdAt = LocalDateTime.now();
        quote.stockItems.forEach(item -> item.setQuote(quote));
        quote.serviceItems.forEach(item -> item.setQuote(quote));
        return quote;
    }

    public void approve() {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_APPROVE, this.status);
        }
        this.status = QuoteStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reprove(String reason) {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_REPROVE, this.status);
        }
        this.status = QuoteStatus.REPROVED;
        this.quoteRefusalReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    private static BigDecimal calculateTotal(List<QuoteStockItem> stockItems) {
       return stockItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /* Comentando essa versão até termos tudo do serviço
    private static BigDecimal calculateTotal(List<QuoteStockItem> stockItems,
                                             List<QuoteServiceItem> serviceItems) {
        BigDecimal stockTotal = stockItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal serviceTotal = serviceItems.stream()
                .map(QuoteServiceItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return stockTotal.add(serviceTotal);
    }
    */

}
