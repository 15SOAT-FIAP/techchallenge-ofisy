package br.com.ofisy.application.notification.ports.in;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record CreateQuoteCommand(
    UUID quoteId,
    UUID serviceOrderId,
    BigDecimal totalPrice
) {
    public CreateQuoteCommand {
        if (quoteId == null) throw new IllegalArgumentException("quoteId não pode ser nulo");
        if (serviceOrderId == null) throw new IllegalArgumentException("serviceOrderId não pode ser nulo");
        if (totalPrice == null) throw new IllegalArgumentException("totalPrice não pode ser nulo");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateQuoteCommand that = (CreateQuoteCommand) o;
        return Objects.equals(quoteId, that.quoteId) && 
               Objects.equals(serviceOrderId, that.serviceOrderId) && 
               Objects.equals(totalPrice, that.totalPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteId, serviceOrderId, totalPrice);
    }
}
