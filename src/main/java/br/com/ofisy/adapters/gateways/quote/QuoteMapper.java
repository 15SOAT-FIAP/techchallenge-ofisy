package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteMapper {

    public static Quote toDomain(QuoteEntity entity,
                                 StockRepository stockRepository,
                                 ServiceOrderExecutionRepository executionRepository) {
        return Quote.reconstruct(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getQuoteRefusalReason(),
                entity.getStockItems().stream()
                        .map(item -> QuoteStockItemMapper.toDomain(item, stockRepository))
                        .toList(),
                entity.getServiceItems().stream()
                        .map(item -> QuoteServiceItemMapper.toDomain(item, executionRepository))
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

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
                .map(item -> QuoteStockItemMapper.toEntity(item, entity))
                .toList();

        List<QuoteServiceItemEntity> serviceEntities = quote.getServiceItems().stream()
                .map(item -> QuoteServiceItemMapper.toEntity(item, entity))
                .toList();

        entity.getStockItems().addAll(stockEntities);
        entity.getServiceItems().addAll(serviceEntities);

        return entity;
    }
}