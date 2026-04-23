package br.com.ofisy.infrastructure.persistence.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuoteRepositoryImpl implements QuoteRepository {

    private final JpaQuoteRepository jpaQuoteRepository;

    @Override
    public Quote save(Quote quote) {
        return jpaQuoteRepository.save(quote);
    }

    @Override
    public Optional<Quote> findById(UUID id) {
        return jpaQuoteRepository.findById(id);
    }

    @Override
    public List<Quote> findByServiceOrderId(UUID serviceOrderId) {
        return jpaQuoteRepository.findByServiceOrderId(serviceOrderId);
    }
}
