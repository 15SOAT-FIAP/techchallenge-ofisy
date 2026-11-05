package br.com.ofisy.application.quote.update;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stock.release.ReleaseStockUseCase;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class UpdateQuoteService implements UpdateQuoteUseCase {

    public static final String ACTION_EDIT = "editar";
    private final QuoteRepository quoteRepository;
    private final StockRepository stockRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ConsumeStockUseCase consumeStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;

    public UpdateQuoteService(QuoteRepository quoteRepository,
                              StockRepository stockRepository,
                              ServiceCatalogRepository serviceCatalogRepository,
                              ConsumeStockUseCase consumeStockUseCase,
                              ReleaseStockUseCase releaseStockUseCase) {
        this.quoteRepository = quoteRepository;
        this.stockRepository = stockRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.consumeStockUseCase = consumeStockUseCase;
        this.releaseStockUseCase = releaseStockUseCase;
    }

    @Override
    public Quote execute(UpdateQuoteCommand cmd) {
        Quote quote = quoteRepository.findById(cmd.quoteId()).orElseThrow(() -> new QuoteNotFoundException(cmd.quoteId()));

        if (!QuoteStatus.PENDING.equals(quote.getStatus())) {
            throw new InvalidQuoteStatusException(ACTION_EDIT, quote.getStatus());
        }

        var stockCommands = cmd.stockItems() != null ? cmd.stockItems() : List.<CreateQuoteUseCase.StockItemCommand>of();
        var serviceCommands = cmd.serviceItems() != null ? cmd.serviceItems() : List.<CreateQuoteUseCase.ServiceItemCommand>of();

        if (stockCommands.isEmpty() && serviceCommands.isEmpty()) {
            throw new InvalidQuoteItemException("O orçamento deve conter ao menos um item de estoque ou serviço.");
        }

        List<QuoteStockItem> stockItems = buildStockItems(quote, stockCommands);
        List<QuoteServiceItem> serviceItems = buildServiceItems(quote, serviceCommands);

        quote.update(stockItems, serviceItems);
        return quoteRepository.save(quote);
    }

    private List<QuoteStockItem> buildStockItems(Quote quote, List<CreateQuoteUseCase.StockItemCommand> commands) {
        Map<UUID, QuoteStockItem> existingByStockId = quote.getStockItems().stream()
                .collect(Collectors.toMap(QuoteStockItem::getStockId, Function.identity()));

        List<QuoteStockItem> items = new ArrayList<>();
        Set<UUID> requestedStockIds = new HashSet<>();

        for (CreateQuoteUseCase.StockItemCommand command : commands) {
            Stock stock = stockRepository.findById(command.stockId()).orElseThrow(() -> new StockNotFoundException(command.stockId()));

            boolean duplicate = !requestedStockIds.add(command.stockId());
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(stock.getProductName());
            }

            QuoteStockItem existing = existingByStockId.get(command.stockId());

            if (existing != null) {
                int delta = command.quantity() - existing.getQuantity();
                if (delta > 0) {
                    consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(command.stockId(), delta));
                } else if (delta < 0) {
                    releaseStockUseCase.execute(new ReleaseStockUseCase.ReleaseStockCommand(command.stockId(), -delta));
                }

                items.add(QuoteStockItem.reconstruct(
                        existing.getId(), existing.getStockId(), existing.getProductName(),
                        existing.getUnitPrice(), command.quantity(),
                        existing.getCreatedAt(), LocalDateTime.now()));
            } else {
                consumeStockUseCase.execute(
                        new ConsumeStockUseCase.ConsumeStockCommand(command.stockId(), command.quantity()));
                items.add(QuoteStockItem.create(
                        stock.getId(), stock.getProductName(), stock.getUnitPrice(), command.quantity()));
            }
        }

        releaseRemovedStockItems(quote, requestedStockIds);

        return items;
    }

    private void releaseRemovedStockItems(Quote quote, Set<UUID> requestedStockIds) {
        for (QuoteStockItem item : quote.getStockItems()) {
            UUID stockId = item.getStockId();
            if (!requestedStockIds.contains(stockId)) {
                releaseStockUseCase.execute(new ReleaseStockUseCase.ReleaseStockCommand(stockId, item.getQuantity()));
            }
        }
    }

    private List<QuoteServiceItem> buildServiceItems(Quote quote,
                                                     List<CreateQuoteUseCase.ServiceItemCommand> commands) {
        Map<UUID, QuoteServiceItem> existingByServiceCatalogId = quote.getServiceItems().stream()
                .collect(Collectors.toMap(QuoteServiceItem::getServiceCatalogId, Function.identity()));

        List<QuoteServiceItem> items = new ArrayList<>();
        Set<UUID> requestedCatalogIds = new HashSet<>();

        for (CreateQuoteUseCase.ServiceItemCommand command : commands) {
            ServiceCatalog catalog = serviceCatalogRepository.findById(command.serviceCatalogId())
                    .orElseThrow(() -> new ServiceCatalogNotFoundException(String.valueOf(command.serviceCatalogId())));

            boolean duplicate = !requestedCatalogIds.add(command.serviceCatalogId());
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(catalog.getName());
            }

            QuoteServiceItem existing = existingByServiceCatalogId.get(command.serviceCatalogId());
            if (existing != null) {
                items.add(QuoteServiceItem.reconstruct(
                        existing.getId(), existing.getServiceCatalogId(), existing.getPrice(),
                        existing.getCreatedAt(), LocalDateTime.now()));
            } else {
                items.add(QuoteServiceItem.create(command.serviceCatalogId(), catalog.getPrice()));
            }
        }

        return items;
    }
}