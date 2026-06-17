package br.com.ofisy.application.quote.findbyid;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindQuoteByIdService implements FindQuoteByIdUseCase {

    private final QuoteRepository quoteRepository;

    @Override
    @Transactional(readOnly = true)
    public Quote execute(UUID id) {
        return quoteRepository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));
    }
}
