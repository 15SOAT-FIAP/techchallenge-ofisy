package br.com.ofisy.application.quote.findbyserviceorderid;

import br.com.ofisy.domain.quote.Quote;

import java.util.List;
import java.util.UUID;

public interface FindQuoteByServiceOrderIdUseCase {
    List<Quote> execute(UUID serviceOrderId);
}
