package br.com.ofisy.application.stock.consume;

import br.com.ofisy.domain.stock.Stock;

import java.util.UUID;

public interface ConsumeStockUseCase {

    Stock execute(ConsumeStockCommand cmd);

    record ConsumeStockCommand(
            UUID stockId,
            Integer quantity
    ) {}
}
