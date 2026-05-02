package br.com.ofisy.infrastructure.persistence.quote;

import br.com.ofisy.domain.quote.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaQuoteRepository extends JpaRepository<Quote, UUID> {

    List<Quote> findByServiceOrderId(UUID serviceOrderId);

    boolean existsByServiceOrderId(UUID serviceOrderId);
}