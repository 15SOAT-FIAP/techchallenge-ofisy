package br.com.ofisy.application.stock.add;

import br.com.ofisy.domain.stock.Stock;

import java.util.UUID;

public interface AddStockUseCase {

    Stock execute(AddStockCommand cmd);

    record AddStockCommand(
            UUID stockId,
            Integer quantity
    ) {}
}
