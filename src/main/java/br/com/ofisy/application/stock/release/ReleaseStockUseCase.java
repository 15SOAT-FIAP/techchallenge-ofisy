package br.com.ofisy.application.stock.release;

import br.com.ofisy.domain.stock.Stock;

import java.util.UUID;

public interface ReleaseStockUseCase {

    Stock execute(ReleaseStockCommand cmd);

    record ReleaseStockCommand(
            UUID stockId,
            Integer quantity
    ) {}
}