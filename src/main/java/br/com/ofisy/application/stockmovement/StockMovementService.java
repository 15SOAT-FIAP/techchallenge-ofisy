package br.com.ofisy.application.stockmovement;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StockMovementService {

    StockMovementRepository stockMovementRepository;

    public StockMovement registerMovement(UUID stockId, MovementType type, Integer quantity,
                                 Integer previousQuantity, Integer newQuantity) {

        StockMovement stockMovement = StockMovement.create(
                stockId,
                type,
                quantity,
                previousQuantity,
                newQuantity
        );

        return stockMovementRepository.save(stockMovement);
    }
}
