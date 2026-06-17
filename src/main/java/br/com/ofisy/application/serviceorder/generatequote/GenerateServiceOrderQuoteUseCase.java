package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.domain.quote.Quote;

import java.util.List;
import java.util.UUID;

public interface GenerateServiceOrderQuoteUseCase {

    Quote execute(GenerateQuoteCommand cmd);

    record GenerateQuoteCommand(
            UUID serviceOrderId,
            List<CreateQuoteUseCase.StockItemCommand> stockItems,
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems
    ) {}
}