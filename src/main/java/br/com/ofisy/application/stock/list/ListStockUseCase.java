package br.com.ofisy.application.stock.list;

import br.com.ofisy.domain.stock.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListStockUseCase {

    Page<Stock> execute(Pageable pageable);
}
