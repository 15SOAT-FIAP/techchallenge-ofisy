package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteMapper {

    public static Quote toDomain(QuoteEntity entity) {
        return Quote.reconstruct(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getQuoteRefusalReason(),
                entity.getStockItems().stream()
                        .map(QuoteStockItemMapper::toDomain)
                        .toList(),
                entity.getServiceItems().stream()
                        .map(QuoteServiceItemMapper::toDomain)
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