package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteStockItem {

    private UUID id;
    private Quote quote;
    private Stock stock;
    private BigDecimal unitPrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuoteStockItem create(Stock stock, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidQuoteItemException("Quantidade do item deve ser maior que zero!");
        }
        QuoteStockItem item = new QuoteStockItem();
        item.stock = stock;
        item.unitPrice = stock.getUnitPrice();
        item.quantity = quantity;
        item.createdAt = LocalDateTime.now();
        item.updatedAt = LocalDateTime.now();
        return item;
    }

    public static QuoteStockItem reconstruct(UUID id, Stock stock, BigDecimal unitPrice,
                                             Integer quantity, LocalDateTime createdAt,
                                             LocalDateTime updatedAt) {
        QuoteStockItem item = new QuoteStockItem();
        item.id = id;
        item.stock = stock;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        item.createdAt = createdAt;
        item.updatedAt = updatedAt;
        return item;
    }

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}