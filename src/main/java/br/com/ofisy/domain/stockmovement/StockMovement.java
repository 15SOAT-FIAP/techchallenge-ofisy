package br.com.ofisy.domain.stockmovement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private UUID stockId;

    @Column
    private MovementType movementType;

    @Column
    private Integer quantity;

    @Column
    private Integer previousQuantity;

    @Column
    private Integer newQuantity;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    private StockMovement(UUID stockId, MovementType movementType, Integer quantity, Integer previousQuantity, Integer newQuantity) {
        this.stockId = stockId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static StockMovement create(UUID stockId, MovementType movementType, Integer quantity, Integer previousQuantity, Integer newQuantity) {
        return new StockMovement(stockId, movementType, quantity, previousQuantity, newQuantity);
    }
}
