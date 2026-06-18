package br.com.ofisy.adapters.gateways.stockmovement;

import br.com.ofisy.domain.stockmovement.StockMovement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMovementMapper {

    public static StockMovement toDomain(StockMovementEntity entity) {
        return StockMovement.reconstruct(
                entity.getId(),
                entity.getStockId(),
                entity.getMovementType(),
                entity.getQuantity(),
                entity.getPreviousQuantity(),
                entity.getNewQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static StockMovementEntity toEntity(StockMovement domain) {
        return StockMovementEntity.builder()
                .id(domain.getId())
                .stockId(domain.getStockId())
                .movementType(domain.getMovementType())
                .quantity(domain.getQuantity())
                .previousQuantity(domain.getPreviousQuantity())
                .newQuantity(domain.getNewQuantity())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
