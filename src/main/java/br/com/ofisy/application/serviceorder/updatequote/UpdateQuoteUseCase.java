package br.com.ofisy.application.serviceorder.updatequote;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.domain.quote.Quote;

import java.util.List;
import java.util.UUID;

public interface UpdateQuoteUseCase {

    Quote execute(UpdateQuoteCommand cmd);

    record UpdateQuoteCommand(
            UUID quoteId,
            List<CreateQuoteUseCase.StockItemCommand> stockItems,
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems
    ) {}
}
