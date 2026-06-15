package br.com.ofisy.adapters.gateways.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaStockRepository extends JpaRepository<StockEntity, UUID> {

    Optional<StockEntity> findByProductName(String value);
}
