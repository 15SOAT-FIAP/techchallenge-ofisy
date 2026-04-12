package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService (StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock addStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        stock.addQuantity(quantity);

        return stockRepository.save(stock);
    }

    public Stock consumeStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        if (stock.getQuantity() == null || stock.getQuantity() < quantity) {
            throw new InsufficientStockException(stockId);
        }

        stock.consumeQuantity(quantity);

        return stockRepository.save(stock);
    }
}
