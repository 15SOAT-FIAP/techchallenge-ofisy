package br.com.ofisy.adapters.gateways.quote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaQuoteRepository extends JpaRepository<QuoteEntity, UUID> {

    List<QuoteEntity> findByServiceOrderId(UUID serviceOrderId);

    boolean existsByServiceOrderId(UUID serviceOrderId);
}
