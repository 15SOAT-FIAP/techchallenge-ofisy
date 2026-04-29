package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_stock_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quote_id", "stock_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteStockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
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

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}