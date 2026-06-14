package br.com.ofisy.application.stock.update;

import br.com.ofisy.domain.stock.Stock;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateStockUseCase {

    Stock execute(UpdateStockCommand cmd);

    record UpdateStockCommand(
            UUID stockId,
            String productName,
            String description,
            BigDecimal unitPrice,
            String category,
            Integer minThreshold
    ) {}
}
