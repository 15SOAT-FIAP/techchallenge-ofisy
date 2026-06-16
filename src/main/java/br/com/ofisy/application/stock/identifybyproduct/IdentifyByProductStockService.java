package br.com.ofisy.application.stock.identifybyproduct;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentifyByProductStockService implements IdentifyByProductStockUseCase {

    private final StockRepository stockRepository;

    public IdentifyByProductStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock execute(String productName) {
        return stockRepository.findByProductName(productName)
                .orElseThrow(() -> new StockNotFoundException(productName));
    }
}
