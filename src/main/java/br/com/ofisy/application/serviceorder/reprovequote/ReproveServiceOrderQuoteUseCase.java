package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.domain.quote.Quote;

import java.util.UUID;

public interface ReproveServiceOrderQuoteUseCase {

    Quote execute(ReproveServiceOrderQuoteCommand cmd);

    record ReproveServiceOrderQuoteCommand(
            UUID quoteId,
            String reason
    ) {}
}