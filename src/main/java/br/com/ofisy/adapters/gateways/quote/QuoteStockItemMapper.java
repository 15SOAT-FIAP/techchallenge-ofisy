package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteStockItemMapper {

    public static QuoteStockItem toDomain(QuoteStockItemEntity entity, StockRepository stockRepository) {
        Stock stock = stockRepository.findById(entity.getStockId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estoque com id " + entity.getStockId() + " não encontrado"));

        return QuoteStockItem.reconstruct(
                entity.getId(),
                stock,
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
                .stockId(item.getStock().getId())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}