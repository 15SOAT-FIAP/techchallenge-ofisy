package br.com.ofisy.infrastructure.persistence.stock;

import br.com.ofisy.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaStockRepository extends JpaRepository<Stock, UUID> {

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.minThreshold")
    List<Stock> findAllBelowThreshold();
}
