package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quote_service_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quote_id", "service_order_executions_id"})
})

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_executions_id", nullable = false)
    private ServiceOrderExecution serviceOrderExecution;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public static QuoteServiceItem create(ServiceOrderExecution serviceOrderExecution, BigDecimal servicePrice) {
        if (servicePrice == null || servicePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuoteItemException("Preço do serviço deve ser maior que zero!");
        }
        QuoteServiceItem item = new QuoteServiceItem();
        item.serviceOrderExecution = serviceOrderExecution;
        item.price = servicePrice;
        item.createdAt = LocalDateTime.now();
        item.updatedAt = LocalDateTime.now();
        return item;
    }

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}
