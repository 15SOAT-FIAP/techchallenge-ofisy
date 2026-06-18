package br.com.ofisy.application.stockmovement.register;

import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterStockMovementService implements RegisterStockMovementUseCase {

    private final StockMovementRepository stockMovementRepository;

    public RegisterStockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public StockMovement execute(RegisterStockMovementCommand cmd) {
        StockMovement stockMovement = StockMovement.create(
                cmd.stockId(),
                cmd.type(),
                cmd.quantity(),
                cmd.previousQuantity(),
                cmd.newQuantity()
        );
        return stockMovementRepository.save(stockMovement);
    }
}
