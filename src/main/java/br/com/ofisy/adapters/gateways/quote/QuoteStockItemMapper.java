package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteStockItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteStockItemMapper {

    public static QuoteStockItem toDomain(QuoteStockItemEntity entity) {
        return QuoteStockItem.reconstruct(
                entity.getId(),
                entity.getStockId(),
                entity.getProductName(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static QuoteStockItemEntity toEntity(QuoteStockItem item, QuoteEntity quoteEntity) {
        return QuoteStockItemEntity.builder()
                .id(item.getId())
                .quote(quoteEntity)
                .stockId(item.getStockId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
