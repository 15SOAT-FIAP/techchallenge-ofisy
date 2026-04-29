package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.QuoteServiceItemResponseDTO;
import br.com.ofisy.application.quote.dto.QuoteStockItemResponseDTO;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class QuoteMapper {

    public QuoteResponseDTO toResponse(Quote quote) {
        return new QuoteResponseDTO(
                quote.getId(),
                quote.getServiceOrderId(),
                quote.getStatus(),
                quote.getTotalPrice(),
                quote.getQuoteRefusalReason(),
                quote.getStockItems().stream().map(this::toStockItemResponse).toList(),
                quote.getServiceItems().stream().map(this::toServiceItemResponse).toList(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }

    private QuoteStockItemResponseDTO toStockItemResponse(QuoteStockItem item) {
        return new QuoteStockItemResponseDTO(
                item.getId(),
                item.getStock().getId(),
                item.getStock().getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                item.getCreatedAt()
        );
    }

    private QuoteServiceItemResponseDTO toServiceItemResponse(QuoteServiceItem item) {
        return new QuoteServiceItemResponseDTO(
                item.getId(),
                item.getServiceOrderExecution().getId(),
                item.getPrice(),
                item.getCreatedAt()
        );
    }
}
