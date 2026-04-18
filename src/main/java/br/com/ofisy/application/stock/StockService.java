package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
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

    private final StockMovementService stockMovementService;

    private final StockRepository stockRepository;

    @Transactional
    public StockResponseDTO create(CreateStockRequestDTO createStockRequestDTO) {
        Stock stock = StockMapper.toDomain(createStockRequestDTO);

        Stock savedStock = stockRepository.save(stock);

        return StockMapper.toDTO(savedStock);
    }

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
        Integer newQuantity = stock.getQuantity() - quantity;

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
