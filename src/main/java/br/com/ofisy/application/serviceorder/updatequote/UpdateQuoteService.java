package br.com.ofisy.application.serviceorder.updatequote;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UpdateQuoteService implements UpdateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final StockRepository stockRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    private final ConsumeStockUseCase consumeStockUseCase;

    public UpdateQuoteService(QuoteRepository quoteRepository,
                              StockRepository stockRepository,
                              ServiceCatalogRepository serviceCatalogRepository,
                              ServiceOrderExecutionRepository serviceOrderExecutionRepository,
                              ConsumeStockUseCase consumeStockUseCase) {
        this.quoteRepository = quoteRepository;
        this.stockRepository = stockRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.serviceOrderExecutionRepository = serviceOrderExecutionRepository;
        this.consumeStockUseCase = consumeStockUseCase;
    }

    @Override
    public Quote execute(UpdateQuoteCommand cmd) {
        Quote quote = quoteRepository.findById(cmd.quoteId())
                .orElseThrow(() -> new QuoteNotFoundException(cmd.quoteId()));

        if (!QuoteStatus.PENDING.equals(quote.getStatus())) {
            throw new InvalidQuoteStatusException("editar", quote.getStatus());
        }

        List<QuoteStockItem> stockItems = buildStockItems(
                cmd.stockItems() != null ? cmd.stockItems() : List.of());

        List<QuoteServiceItem> serviceItems = buildServiceItems(
                quote.getServiceOrderId(),
                cmd.serviceItems() != null ? cmd.serviceItems() : List.of());

        quote.update(stockItems, serviceItems);
        return quoteRepository.save(quote);
    }

    private List<QuoteStockItem> buildStockItems(List<CreateQuoteUseCase.StockItemCommand> commands) {
        List<QuoteStockItem> items = new ArrayList<>();

        for (CreateQuoteUseCase.StockItemCommand command : commands) {
            Stock stock = stockRepository.findById(command.stockId())
                    .orElseThrow(() -> new StockNotFoundException(command.stockId()));

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getStock().getId().equals(command.stockId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(stock.getProductName());
            }

            consumeStockUseCase.execute(
                    new ConsumeStockUseCase.ConsumeStockCommand(command.stockId(), command.quantity()));
            items.add(QuoteStockItem.create(stock, command.quantity()));
        }

        return items;
    }

    private List<QuoteServiceItem> buildServiceItems(java.util.UUID serviceOrderId,
                                                      List<CreateQuoteUseCase.ServiceItemCommand> commands) {
        List<QuoteServiceItem> items = new ArrayList<>();

        for (CreateQuoteUseCase.ServiceItemCommand command : commands) {
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
