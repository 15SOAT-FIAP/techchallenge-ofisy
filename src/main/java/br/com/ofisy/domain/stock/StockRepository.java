package br.com.ofisy.domain.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository {

    Stock save(Stock stock);

    Page<Stock> findAll(Pageable pageable);

    Optional<Stock> findById(UUID id);

    Optional<Stock> findByProductName(String productName);
}
