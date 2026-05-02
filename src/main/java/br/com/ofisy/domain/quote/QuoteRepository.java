package br.com.ofisy.domain.quote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository {

    Quote save(Quote quote);

    Optional<Quote> findById(UUID id);

    List<Quote> findByServiceOrderId(UUID serviceOrderId);

    boolean existsByServiceOrderId(UUID serviceOrderId);
}
