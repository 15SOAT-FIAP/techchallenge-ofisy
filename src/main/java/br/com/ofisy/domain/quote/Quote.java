package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Quote {

    public static final String ACTION_APPROVE = "aprovar";
    public static final String ACTION_REPROVE = "reprovar";
    public static final String ACTION_EDIT = "editar";

    private UUID id;
    private UUID serviceOrderId;
    private QuoteStatus status;
    private BigDecimal totalPrice;
    private List<QuoteStockItem> stockItems = new ArrayList<>();
    private List<QuoteServiceItem> serviceItems = new ArrayList<>();
    private String quoteRefusalReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Quote create(UUID serviceOrderId,
                               List<QuoteStockItem> stockItems,
                               List<QuoteServiceItem> serviceItems) {
        Quote quote = new Quote();
        quote.serviceOrderId = serviceOrderId;
        quote.status = QuoteStatus.PENDING;
        quote.stockItems = stockItems != null ? stockItems : new ArrayList<>();
        quote.serviceItems = serviceItems != null ? serviceItems : new ArrayList<>();

        if (quote.stockItems.isEmpty() && quote.serviceItems.isEmpty()) {
            throw new InvalidQuoteDataException(serviceOrderId);
        }

        quote.totalPrice = calculateTotal(quote.stockItems, quote.serviceItems);
        quote.createdAt = LocalDateTime.now();
        quote.updatedAt = LocalDateTime.now();
        quote.stockItems.forEach(item -> item.setQuote(quote));
        quote.serviceItems.forEach(item -> item.setQuote(quote));
        return quote;
    }

    public static Quote reconstruct(UUID id,
                                    UUID serviceOrderId,
                                    QuoteStatus status,
                                    BigDecimal totalPrice,
                                    String quoteRefusalReason,
                                    List<QuoteStockItem> stockItems,
                                    List<QuoteServiceItem> serviceItems,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        Quote quote = new Quote();
        quote.id = id;
        quote.serviceOrderId = serviceOrderId;
        quote.status = status;
        quote.totalPrice = totalPrice;
        quote.quoteRefusalReason = quoteRefusalReason;
        quote.stockItems = stockItems != null ? stockItems : new ArrayList<>();
        quote.serviceItems = serviceItems != null ? serviceItems : new ArrayList<>();
        quote.createdAt = createdAt;
        quote.updatedAt = updatedAt;
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

    public void update(List<QuoteStockItem> newStockItems, List<QuoteServiceItem> newServiceItems) {
        if (!QuoteStatus.PENDING.equals(this.status)) {
            throw new InvalidQuoteStatusException(ACTION_EDIT, this.status);
        }
        if ((newStockItems == null || newStockItems.isEmpty())
                && (newServiceItems == null || newServiceItems.isEmpty())) {
            throw new InvalidQuoteDataException(this.serviceOrderId);
        }
        this.stockItems = newStockItems != null ? newStockItems : new ArrayList<>();
        this.serviceItems = newServiceItems != null ? newServiceItems : new ArrayList<>();
        this.totalPrice = calculateTotal(this.stockItems, this.serviceItems);
        this.stockItems.forEach(item -> item.setQuote(this));
        this.serviceItems.forEach(item -> item.setQuote(this));
        this.updatedAt = LocalDateTime.now();
    }

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
}