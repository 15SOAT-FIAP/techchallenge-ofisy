package br.com.ofisy.adapters.gateways.stock;

import br.com.ofisy.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMapper {

    public static Stock toDomain(StockEntity entity) {
        return Stock.reconstruct(
                entity.getId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getCategory(),
                entity.getMinThreshold(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static StockEntity toEntity(Stock stock) {
        return StockEntity.builder()
                .id(stock.getId())
                .productName(stock.getProductName())
                .description(stock.getDescription())
                .quantity(stock.getQuantity())
                .unitPrice(stock.getUnitPrice())
                .category(stock.getCategory())
                .minThreshold(stock.getMinThreshold())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
