package br.com.ofisy.application.stockmovement.listbystock;

import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListByStockStockMovementService implements ListByStockStockMovementUseCase {

    private final StockMovementRepository stockMovementRepository;

    public ListByStockStockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public Page<StockMovement> execute(UUID stockId, Pageable pageable) {
        return stockMovementRepository.findByStockId(stockId, pageable);
    }
}
