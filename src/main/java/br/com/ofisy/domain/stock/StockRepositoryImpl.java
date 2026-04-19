package br.com.ofisy.domain.stock;

import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class StockRepositoryImpl implements StockRepository {

    private final Map<UUID, Stock> store = new HashMap<>();

    @Override
    public Stock save(Stock stock) {
        store.put(stock.getId(), stock);
        return stock;
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}