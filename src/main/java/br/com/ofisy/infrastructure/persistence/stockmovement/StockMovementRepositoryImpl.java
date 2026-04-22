package br.com.ofisy.infrastructure.persistence.stockmovement;

import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final JpaStockMovementRepository jpa;

    @Override
    public StockMovement save(StockMovement stockMovement) {
        return jpa.save(stockMovement);
    }

    @Override
    public Page<StockMovement> findByStockId(UUID stockId, Pageable pageable) {
        return jpa.findByStockId(stockId, pageable);
    }

    @Override
    public Page<StockMovement> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}
