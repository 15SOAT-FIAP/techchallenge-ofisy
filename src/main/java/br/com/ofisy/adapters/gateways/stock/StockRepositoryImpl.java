package br.com.ofisy.adapters.gateways.stock;

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
        StockEntity entity = StockMapper.toEntity(stock);
        return StockMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Page<Stock> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(StockMapper::toDomain);
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        return jpa.findById(id).map(StockMapper::toDomain);
    }

    @Override
    public Optional<Stock> findByProductName(String productName) {
        return jpa.findByProductName(productName).map(StockMapper::toDomain);
    }
}
