package br.com.ofisy.application.serviceorder.createcomplete;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.List;
import java.util.UUID;

public interface CreateCompleteServiceOrderUseCase {

    ServiceOrder execute(CreateServiceOrderCommand cmd);

    record CreateServiceOrderCommand(
            UUID vehicleId,
            UUID customerId,
            String report,
            String createdByEmail,
            List<CreateQuoteUseCase.StockItemCommand> stockItems,
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems
    ) {}
}