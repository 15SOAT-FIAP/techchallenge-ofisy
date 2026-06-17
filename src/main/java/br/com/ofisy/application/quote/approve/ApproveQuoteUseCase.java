package br.com.ofisy.application.quote.approve;

import br.com.ofisy.domain.quote.Quote;

import java.util.UUID;

public interface ApproveQuoteUseCase {
    Quote execute(UUID id);
}
