package br.com.ofisy.application.stock.create;

import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateStockService implements CreateStockUseCase {

    private final StockRepository stockRepository;

    public CreateStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock execute(CreateStockCommand cmd) {
        Stock stock = Stock.create(
                cmd.productName(),
                cmd.description(),
                cmd.quantity(),
                cmd.unitPrice(),
                cmd.category(),
                cmd.minThreshold()
        );
        return stockRepository.save(stock);
    }
}
