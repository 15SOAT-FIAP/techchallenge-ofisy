package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    public static QuoteStockItem create(Stock stock, Integer quantity) {
        QuoteStockItem item = new QuoteStockItem();
        item.stock = stock;
        item.unitPrice = stock.getUnitPrice();
        item.quantity = quantity;
        return item;
    }

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}