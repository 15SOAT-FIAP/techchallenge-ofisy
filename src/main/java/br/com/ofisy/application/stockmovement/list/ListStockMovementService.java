package br.com.ofisy.application.stockmovement.list;

import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListStockMovementService implements ListStockMovementUseCase {

    private final StockMovementRepository stockMovementRepository;

    public ListStockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public Page<StockMovement> execute(Pageable pageable) {
        return stockMovementRepository.findAll(pageable);
    }
}
