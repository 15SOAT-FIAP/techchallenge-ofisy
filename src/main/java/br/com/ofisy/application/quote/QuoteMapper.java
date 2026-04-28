package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.QuoteServiceItemResponseDTO;
import br.com.ofisy.application.quote.dto.QuoteStockItemResponseDTO;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

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
                UUID.randomUUID(), //aqui assim até termos a parte de serviços
                new BigDecimal(1), //aqui assim até termos a parte de serviços
                item.getCreatedAt()
                //quando tivermos a parte de serviços descomentar abaixo
                //item.serviceOrderExecution().getId(),
               // item.getPrice()
        );
    }
}
