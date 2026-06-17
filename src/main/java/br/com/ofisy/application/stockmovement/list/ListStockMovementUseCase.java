package br.com.ofisy.application.stockmovement.list;

import br.com.ofisy.domain.stockmovement.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListStockMovementUseCase {

    Page<StockMovement> execute(Pageable pageable);
}
