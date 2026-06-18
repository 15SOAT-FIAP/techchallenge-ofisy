package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteServiceItem {

    private UUID id;
    private Quote quote;
    private ServiceOrderExecution serviceOrderExecution;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public static QuoteServiceItem reconstruct(UUID id, ServiceOrderExecution serviceOrderExecution,
                                               BigDecimal price, LocalDateTime createdAt,
                                               LocalDateTime updatedAt) {
        QuoteServiceItem item = new QuoteServiceItem();
        item.id = id;
        item.serviceOrderExecution = serviceOrderExecution;
        item.price = price;
        item.createdAt = createdAt;
        item.updatedAt = updatedAt;
        return item;
    }

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}