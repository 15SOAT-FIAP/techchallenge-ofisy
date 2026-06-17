package br.com.ofisy.application.quote.findbyid;

import br.com.ofisy.domain.quote.Quote;

import java.util.UUID;

public interface FindQuoteByIdUseCase {
    Quote execute(UUID id);
}
