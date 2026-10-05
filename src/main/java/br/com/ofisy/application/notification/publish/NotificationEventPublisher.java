package br.com.ofisy.application.notification.publish;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationEventPublisher {

    void publishLowStock(LowStockEvent event);

    void publishQuoteGenerated(QuoteGeneratedEvent event);

    record LowStockEvent(UUID stockId, String productName, Integer currentQuantity, Integer minThreshold) {}

    record QuoteGeneratedEvent(UUID quoteId, UUID serviceOrderId, BigDecimal totalPrice) {}
}
