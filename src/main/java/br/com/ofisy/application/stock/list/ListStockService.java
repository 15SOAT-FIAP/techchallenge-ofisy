package br.com.ofisy.application.stock.list;

import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListStockService implements ListStockUseCase {

    private final StockRepository stockRepository;

    public ListStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Page<Stock> execute(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }
}
