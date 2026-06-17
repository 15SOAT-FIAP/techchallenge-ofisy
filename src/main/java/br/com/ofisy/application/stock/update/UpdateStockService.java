package br.com.ofisy.application.stock.update;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateStockService implements UpdateStockUseCase {

    private final StockRepository stockRepository;

    public UpdateStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock execute(UpdateStockCommand cmd) {
        Stock stock = stockRepository.findById(cmd.stockId())
                .orElseThrow(() -> new StockNotFoundException(cmd.stockId()));

        stock.update(
                cmd.productName(),
                cmd.description(),
                cmd.unitPrice(),
                cmd.category(),
                cmd.minThreshold()
        );

        return stockRepository.save(stock);
    }
}
