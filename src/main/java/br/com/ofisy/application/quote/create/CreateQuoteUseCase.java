package br.com.ofisy.application.quote.create;

import br.com.ofisy.domain.quote.Quote;

import java.util.List;
import java.util.UUID;

public interface CreateQuoteUseCase {

    Quote execute(CreateQuoteCommand cmd);

    record CreateQuoteCommand(
            UUID serviceOrderId,
            List<StockItemCommand> stockItems,
            List<ServiceItemCommand> serviceItems
    ) {}

    record StockItemCommand(
            UUID stockId,
            Integer quantity
    ) {}

    record ServiceItemCommand(
            UUID serviceOrderExecutionId
    ) {}
}