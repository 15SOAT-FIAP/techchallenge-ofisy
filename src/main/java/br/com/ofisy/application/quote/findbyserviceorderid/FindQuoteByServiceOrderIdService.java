package br.com.ofisy.application.quote.findbyserviceorderid;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindQuoteByServiceOrderIdService implements FindQuoteByServiceOrderIdUseCase {

    private final QuoteRepository quoteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Quote> execute(UUID serviceOrderId) {

        return quoteRepository.findByServiceOrderId(serviceOrderId);
    }
}
