package br.com.ofisy.infrastructure.persistence.stock;

import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final JpaStockRepository jpa;

    @Override
    public Stock save(Stock stock) {
        return jpa.save(stock);
    }

    @Override
    public Page<Stock> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Stock> findByProductName(String productName) {
        return jpa.findByProductName(productName);
    }
}
