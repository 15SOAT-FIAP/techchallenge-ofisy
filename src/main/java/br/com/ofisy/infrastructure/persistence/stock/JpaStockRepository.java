package br.com.ofisy.infrastructure.persistence.stock;

import br.com.ofisy.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaStockRepository extends JpaRepository<Stock, UUID> {

    Optional<Stock> findByProductName(String value);
}
