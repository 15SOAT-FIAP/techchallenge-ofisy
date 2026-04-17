package br.com.ofisy.domain.stock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository {

    Stock save(Stock stock);

    Optional<Stock> findById(UUID id);

    List<Stock> findAllBelowThreshold();
}
