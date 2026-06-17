package br.com.ofisy.application.stock.add;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stockmovement.MovementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddStockService implements AddStockUseCase {

    private final StockRepository stockRepository;
    private final StockMovementService stockMovementService;

    public AddStockService(StockRepository stockRepository, StockMovementService stockMovementService) {
        this.stockRepository = stockRepository;
        this.stockMovementService = stockMovementService;
    }

    @Override
    public Stock execute(AddStockCommand cmd) {
        Stock stock = stockRepository.findById(cmd.stockId())
                .orElseThrow(() -> new StockNotFoundException(cmd.stockId()));

        Integer previousQuantity = stock.getQuantity();
        stock.addQuantity(cmd.quantity());
        Integer newQuantity = stock.getQuantity();

        stockMovementService.registerMovement(new StockMovementRequestDTO(
                cmd.stockId(),
                MovementType.IN,
                cmd.quantity(),
                previousQuantity,
                newQuantity
        ));

        return stockRepository.save(stock);
    }
}
