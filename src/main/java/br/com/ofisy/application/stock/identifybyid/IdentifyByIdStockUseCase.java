package br.com.ofisy.application.stock.identifybyid;

import br.com.ofisy.domain.stock.Stock;

import java.util.UUID;

public interface IdentifyByIdStockUseCase {

    Stock execute(UUID id);
}
