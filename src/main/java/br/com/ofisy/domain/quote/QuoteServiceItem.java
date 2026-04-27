package br.com.ofisy.domain.quote;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "quote_service_items", uniqueConstraints = {
        //@UniqueConstraint(columnNames = {"quote_id", "service_order_executions_id"}) - comentando aqui até termos tudo dos serviços
        @UniqueConstraint(columnNames = {"quote_id"})
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

    /* Comentando até termos tudo do serviço comitado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_executions_id", nullable = false)
    private ServiceOrderExecution serviceOrderExecution;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public static QuoteServiceItem create(ServiceOrderExecution serviceOrderExecution, BigDecimal servicePrice) {
        QuoteServiceItem item = new QuoteServiceItem();
        item.serviceOrderExecution = serviceOrderExecution;
        item.price = servicePrice;
        return item;
    } */

    void setQuote(Quote quote) {
        this.quote = quote;
    }
}
