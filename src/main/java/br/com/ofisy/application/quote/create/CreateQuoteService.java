package br.com.ofisy.application.quote.create;

import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateQuoteService implements CreateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;

    @Override
    @Transactional
    public Quote execute(CreateQuoteCommand cmd) {
        if (quoteRepository.existsByServiceOrderId(cmd.serviceOrderId())) {
            throw new QuoteAlreadyExistsException(cmd.serviceOrderId());
        }

        List<QuoteStockItem> stockItems = buildStockItems(cmd.stockItems() != null ? cmd.stockItems() : List.of());
        List<QuoteServiceItem> serviceItems = buildServiceItems(cmd.serviceItems() != null ? cmd.serviceItems() : List.of());

        Quote quote = Quote.create(cmd.serviceOrderId(), stockItems, serviceItems);
        return quoteRepository.save(quote);
    }

    private List<QuoteStockItem> buildStockItems(List<StockItemCommand> commands) {
        List<QuoteStockItem> items = new ArrayList<>();

        for (StockItemCommand command : commands) {
            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getStockId().equals(command.stockId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(command.productName());
            }

            items.add(QuoteStockItem.create(
                    command.stockId(), command.productName(), command.unitPrice(), command.quantity()));
        }

        return items;
    }

    private List<QuoteServiceItem> buildServiceItems(List<ServiceItemCommand> commands) {
        List<QuoteServiceItem> items = new ArrayList<>();

        for (ServiceItemCommand command : commands) {
            ServiceCatalog catalog = serviceCatalogRepository.findById(command.serviceCatalogId())
                    .orElseThrow(() -> new ServiceCatalogNotFoundException(
                            String.valueOf(command.serviceCatalogId())));

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getServiceCatalogId().equals(command.serviceCatalogId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(catalog.getName());
            }

            items.add(QuoteServiceItem.create(command.serviceCatalogId(), catalog.getPrice()));
        }

        return items;
    }
}
