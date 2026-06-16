package br.com.ofisy.application.stock.identifybyid;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IdentifyByIdStockService implements IdentifyByIdStockUseCase {

    private final StockRepository stockRepository;

    public IdentifyByIdStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock execute(UUID id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));
    }
}
