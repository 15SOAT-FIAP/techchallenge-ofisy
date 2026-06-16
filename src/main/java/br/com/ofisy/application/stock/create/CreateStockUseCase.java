package br.com.ofisy.application.stock.create;

import br.com.ofisy.domain.stock.Stock;

import java.math.BigDecimal;

public interface CreateStockUseCase {

    Stock execute(CreateStockCommand cmd);

    record CreateStockCommand(
            String productName,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            String category,
            Integer minThreshold
    ) {}
}
