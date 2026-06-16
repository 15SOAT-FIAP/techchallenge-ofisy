package br.com.ofisy.application.stock.identifybyproduct;

import br.com.ofisy.domain.stock.Stock;

public interface IdentifyByProductStockUseCase {

    Stock execute(String productName);
}
