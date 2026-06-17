package br.com.ofisy.application.stockmovement.listbystock;

import br.com.ofisy.domain.stockmovement.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListByStockStockMovementUseCase {

    Page<StockMovement> execute(UUID stockId, Pageable pageable);
}
