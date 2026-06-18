package br.com.ofisy.domain.stockmovement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMovement {

    private UUID id;

    private UUID stockId;

    private MovementType movementType;

    private Integer quantity;

    private Integer previousQuantity;

    private Integer newQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private StockMovement(UUID stockId, MovementType movementType, Integer quantity, Integer previousQuantity, Integer newQuantity) {
        this.stockId = stockId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
    }

    public static StockMovement create(UUID stockId, MovementType movementType, Integer quantity, Integer previousQuantity, Integer newQuantity) {
        StockMovement stockMovement = new StockMovement(stockId, movementType, quantity, previousQuantity, newQuantity);
        stockMovement.createdAt = LocalDateTime.now();
        stockMovement.updatedAt = LocalDateTime.now();

        return stockMovement;
    }

    public static StockMovement reconstruct(UUID id, UUID stockId, MovementType movementType,
                                            Integer quantity, Integer previousQuantity, Integer newQuantity,
                                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        StockMovement stockMovement = new StockMovement(stockId, movementType, quantity, previousQuantity, newQuantity);
        stockMovement.id = id;
        stockMovement.createdAt = createdAt;
        stockMovement.updatedAt = updatedAt;
        return stockMovement;
    }
}
