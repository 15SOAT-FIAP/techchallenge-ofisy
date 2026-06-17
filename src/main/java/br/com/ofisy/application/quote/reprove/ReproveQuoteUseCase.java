package br.com.ofisy.application.quote.reprove;

import br.com.ofisy.domain.quote.Quote;

import java.util.UUID;

public interface ReproveQuoteUseCase {

    Quote execute(ReproveQuoteCommand cmd);

    record ReproveQuoteCommand(
            UUID id,
            String reason
    ) {}
}
