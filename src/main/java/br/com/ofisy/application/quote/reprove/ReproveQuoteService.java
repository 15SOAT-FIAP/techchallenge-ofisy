package br.com.ofisy.application.quote.reprove;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReproveQuoteService implements ReproveQuoteUseCase {

    private final QuoteRepository quoteRepository;

    @Override
    @Transactional
    public Quote execute(ReproveQuoteCommand cmd) {
        Quote quote = quoteRepository.findById(cmd.id()).orElseThrow(() -> new QuoteNotFoundException(cmd.id()));
        quote.reprove(cmd.reason());
        return quoteRepository.save(quote);
    }
}
