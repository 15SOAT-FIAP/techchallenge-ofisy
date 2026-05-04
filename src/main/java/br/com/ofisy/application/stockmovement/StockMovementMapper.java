package br.com.ofisy.application.stockmovement;

import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.application.stockmovement.dto.StockMovementResponseDTO;
import br.com.ofisy.domain.stockmovement.StockMovement;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class StockMovementMapper {

    public static StockMovement toDomain(StockMovementRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("StockMovementRequestDTO não pode ser nulo");
        }

        return StockMovement.create(
                request.stockId(),
                request.type(),
                request.quantity(),
                request.previousQuantity(),
                request.newQuantity()
        );
    }

    public static StockMovementResponseDTO toDTO(StockMovement stockMovement){
        if (stockMovement == null) {
            throw new IllegalArgumentException("StockMovement não pode ser nulo");
        }

        return new StockMovementResponseDTO(
                stockMovement.getId(),
                stockMovement.getStockId(),
                stockMovement.getMovementType(),
                stockMovement.getQuantity(),
                stockMovement.getPreviousQuantity(),
                stockMovement.getNewQuantity(),
                stockMovement.getCreatedAt(),
                stockMovement.getUpdatedAt()
        );
    }
}
