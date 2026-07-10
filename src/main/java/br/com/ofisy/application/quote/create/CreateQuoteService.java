package br.com.ofisy.application.quote.create;

import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateQuoteService implements CreateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final StockRepository stockRepository;
    private final ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ConsumeStockUseCase consumeStockUseCase;

    @Override
    @Transactional
    public Quote execute(CreateQuoteCommand cmd) {
        if (quoteRepository.existsByServiceOrderId(cmd.serviceOrderId())) {
            throw new QuoteAlreadyExistsException(cmd.serviceOrderId());
        }

        List<QuoteStockItem> stockItems = buildStockItems(cmd.stockItems() != null ? cmd.stockItems() : List.of());
        List<QuoteServiceItem> serviceItems = buildServiceItems(cmd.serviceOrderId(), cmd.serviceItems() != null ? cmd.serviceItems() : List.of());

        Quote quote = Quote.create(cmd.serviceOrderId(), stockItems, serviceItems);
        return quoteRepository.save(quote);
    }

    private List<QuoteStockItem> buildStockItems(List<StockItemCommand> commands) {
        List<QuoteStockItem> items = new ArrayList<>();

        for (StockItemCommand command : commands) {
            Stock stock = stockRepository.findById(command.stockId())
                    .orElseThrow(() -> new StockNotFoundException(command.stockId()));

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getStock().getId().equals(command.stockId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(stock.getProductName());
            }

            consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(command.stockId(), command.quantity()));
            items.add(QuoteStockItem.create(stock, command.quantity()));
        }

        return items;
    }

    private List<QuoteServiceItem> buildServiceItems(UUID serviceOrderId, List<ServiceItemCommand> commands) {
        List<QuoteServiceItem> items = new ArrayList<>();

        for (ServiceItemCommand command : commands) {
            ServiceCatalog catalog = serviceCatalogRepository.findById(command.serviceCatalogId())
                    .orElseThrow(() -> new ServiceCatalogNotFoundException(
                            String.valueOf(command.serviceCatalogId())));

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getServiceOrderExecution().getServiceCatalogId()
                            .equals(command.serviceCatalogId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(catalog.getName());
            }

            ServiceOrderExecution execution = serviceOrderExecutionRepository.save(
                    ServiceOrderExecution.create(command.serviceCatalogId(), serviceOrderId));

            items.add(QuoteServiceItem.create(execution, catalog.getPrice()));
        }

        return items;
    }
}