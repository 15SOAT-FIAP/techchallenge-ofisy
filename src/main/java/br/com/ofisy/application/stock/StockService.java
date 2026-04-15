package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stockmovement.MovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    private final StockMovementService stockMovementService;

    @Transactional
    public Stock addStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        Integer previousQuantity = stock.getQuantity();
        Integer newQuantity = stock.getQuantity() + quantity;

        stock.addQuantity(quantity);

        stockMovementService.registerMovement(
                stockId,
                MovementType.IN,
                quantity,
                previousQuantity,
                newQuantity
        );

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock consumeStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        if (stock.getQuantity() == null || stock.getQuantity() < quantity) {
            throw new InsufficientStockException(stockId);
        }

        Integer previousQuantity = stock.getQuantity();
        Integer newQuantity = stock.getQuantity() + quantity;

        stock.consumeQuantity(quantity);

        stockMovementService.registerMovement(
                stockId,
                MovementType.OUT,
                quantity,
                previousQuantity,
                newQuantity
        );

        return stockRepository.save(stock);
    }
}
