package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteMapper {

    public static QuoteEntity toEntity(Quote quote) {
        QuoteEntity entity = QuoteEntity.builder()
                .id(quote.getId())
                .serviceOrderId(quote.getServiceOrderId())
                .status(quote.getStatus())
                .totalPrice(quote.getTotalPrice())
                .quoteRefusalReason(quote.getQuoteRefusalReason())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();

        List<QuoteStockItemEntity> stockEntities = quote.getStockItems().stream()
                .map(item -> toStockItemEntity(item, entity))
                .toList();

        List<QuoteServiceItemEntity> serviceEntities = quote.getServiceItems().stream()
                .map(item -> toServiceItemEntity(item, entity))
                .toList();

        entity.getStockItems().addAll(stockEntities);
        entity.getServiceItems().addAll(serviceEntities);

        return entity;
    }

    public static Quote toDomain(QuoteEntity entity,
                                 StockRepository stockRepository,
                                 ServiceOrderExecutionRepository executionRepository) {
        List<QuoteStockItem> stockItems = entity.getStockItems().stream()
                .map(item -> toStockItemDomain(item, stockRepository))
                .toList();

        List<QuoteServiceItem> serviceItems = entity.getServiceItems().stream()
                .map(item -> toServiceItemDomain(item, executionRepository))
                .toList();

        return Quote.reconstruct(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getQuoteRefusalReason(),
                stockItems,
                serviceItems,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static QuoteStockItemEntity toStockItemEntity(QuoteStockItem item, QuoteEntity quoteEntity) {
        return QuoteStockItemEntity.builder()
                .id(item.getId())
                .quote(quoteEntity)
                .stockId(item.getStock().getId())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private static QuoteServiceItemEntity toServiceItemEntity(QuoteServiceItem item, QuoteEntity quoteEntity) {
        return QuoteServiceItemEntity.builder()
                .id(item.getId())
                .quote(quoteEntity)
                .serviceOrderExecutionId(item.getServiceOrderExecution().getId())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private static QuoteStockItem toStockItemDomain(QuoteStockItemEntity entity,
                                                     StockRepository stockRepository) {
        Stock stock = stockRepository.findById(entity.getStockId())
                .orElseThrow(() -> new IllegalArgumentException("Estoque com id " + entity.getStockId() + " não encontrado"));
        return QuoteStockItem.reconstruct(
                entity.getId(),
                stock,
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static QuoteServiceItem toServiceItemDomain(QuoteServiceItemEntity entity,
                                                         ServiceOrderExecutionRepository executionRepository) {
        ServiceOrderExecution execution = executionRepository.findById(entity.getServiceOrderExecutionId())
                .orElseThrow(() -> new IllegalArgumentException("Execução com id " + entity.getServiceOrderExecutionId() + " não encontrada"));
        return QuoteServiceItem.reconstruct(
                entity.getId(),
                execution,
                entity.getPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
