package br.com.ofisy.domain.stockmovement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StockMovementTest {

    @Test
    @DisplayName("Deve registrar moviementação de entrada no estoque")
    void shouldRegisterInMovementSuccessfully() {
        UUID stockId = UUID.randomUUID();

        StockMovement movement = StockMovement.create(
                stockId,
                MovementType.IN,
                10,
                90,
                100
        );

        assertEquals(stockId, movement.getStockId());
        assertEquals(MovementType.IN, movement.getMovementType());
        assertEquals(10, movement.getQuantity());
        assertEquals(90, movement.getPreviousQuantity());
        assertEquals(100, movement.getNewQuantity());
        assertNotNull(movement.getCreatedAt());
        assertNotNull(movement.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve registrar moviementação de saída no estoque")
    void shouldRegisterOutMovementSuccessfully() {
        UUID stockId = UUID.randomUUID();

        StockMovement movement = StockMovement.create(
                stockId,
                MovementType.OUT,
                10,
                100,
                90
        );

        assertEquals(stockId, movement.getStockId());
        assertEquals(MovementType.OUT, movement.getMovementType());
        assertEquals(10, movement.getQuantity());
        assertEquals(100, movement.getPreviousQuantity());
        assertEquals(90, movement.getNewQuantity());
    }
}
