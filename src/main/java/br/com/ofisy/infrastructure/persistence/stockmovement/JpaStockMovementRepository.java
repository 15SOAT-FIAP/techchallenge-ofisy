package br.com.ofisy.infrastructure.persistence.stockmovement;

import br.com.ofisy.domain.stockmovement.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaStockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByStockId(UUID stockId, Pageable pageable);
}
