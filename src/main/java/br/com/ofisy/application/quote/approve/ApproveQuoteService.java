package br.com.ofisy.application.quote.approve;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ApproveQuoteService implements ApproveQuoteUseCase {

    private final QuoteRepository quoteRepository;

    public ApproveQuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Override
    public Quote execute(UUID id) {
        Quote quote = quoteRepository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));
        quote.approve();
        return quoteRepository.save(quote);
    }
}
