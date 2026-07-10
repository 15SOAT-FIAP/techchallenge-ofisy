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
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuoteServiceTest {

    private static final UUID QUOTE_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();
    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final UUID SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final UUID STOCK_ID_SECONDARY = UUID.randomUUID();
    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";

    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;
    @Mock
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Mock
    private ConsumeStockUseCase consumeStockUseCase;
    @Mock
    private ReleaseStockUseCase releaseStockUseCase;

    @InjectMocks
    private UpdateQuoteService service;

    @Nested
    class Execute {

        @Test
        void shouldUpdateQuoteWithNewStockItemsSuccessfully() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 3)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getStockItems()).hasSize(1);
            assertThat(result.getStockItems().get(0).getQuantity()).isEqualTo(3);
            verify(consumeStockUseCase).execute(
                    new ConsumeStockUseCase.ConsumeStockCommand(STOCK_ID, 3));
            verify(quoteRepository).save(quote);
        }

        @Test
        void shouldUpdateQuoteWithNewServiceItemsSuccessfully() {
            var quote = pendingQuote();
            var execution = ServiceOrderExecution.create(SERVICE_CATALOG_ID, SERVICE_ORDER_ID);
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(), List.of(new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog()));
            when(serviceOrderExecutionRepository.save(any())).thenReturn(execution);
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getServiceItems()).hasSize(1);
            verify(serviceOrderExecutionRepository).save(any(ServiceOrderExecution.class));
        }

        @Test
        void shouldRecalculateTotalPriceAfterUpdate() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        void shouldThrowWhenQuoteNotFound() {
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID, List.of(), List.of());
            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteNotFoundException.class);
        }

        @Test
        void shouldThrowWhenQuoteIsNotPending() {
            var quote = pendingQuote();
            quote.approve();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }

        @Test
        void shouldThrowWhenStockNotFound() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        void shouldThrowWhenServiceCatalogNotFound() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(), List.of(new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(ServiceCatalogNotFoundException.class);
        }

        @Test
        void shouldThrowWhenDuplicateStockItem() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(
                            new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1),
                            new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)
                    ), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }
    }

    @Nested
    class StockQuantityDelta {

        @Test
        void shouldConsumeOnlyDeltaWhenQuantityIncreases() {
            var existingItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStock(),
                    new BigDecimal(PRICE_100), 1, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(existingItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 3)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(consumeStockUseCase).execute(new ConsumeStockUseCase.ConsumeStockCommand(STOCK_ID, 2));
            verify(releaseStockUseCase, never()).execute(any());
        }

        @Test
        void shouldReleaseOnlyDeltaWhenQuantityDecreases() {
            var existingItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStock(),
                    new BigDecimal(PRICE_100), 3, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(existingItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(releaseStockUseCase).execute(new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID, 2));
            verify(consumeStockUseCase, never()).execute(any());
        }

        @Test
        void shouldNotTriggerStockMovementWhenQuantityUnchanged() {
            var existingItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStock(),
                    new BigDecimal(PRICE_100), 2, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(existingItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(consumeStockUseCase, never()).execute(any());
            verify(releaseStockUseCase, never()).execute(any());
        }

        @Test
        void shouldReleaseFullQuantityWhenStockItemIsRemoved() {
            var keptItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStock(),
                    new BigDecimal(PRICE_100), 2, LocalDateTime.now(), LocalDateTime.now());
            var removedItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStockSecondary(),
                    new BigDecimal(PRICE_100), 4, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(keptItem, removedItem));

            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(releaseStockUseCase).execute(
                    new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID_SECONDARY, 4));
            verify(consumeStockUseCase, never()).execute(any());
        }

        @Test
        void shouldPreserveStockItemIdWhenQuantityChanges() {
            var existingId = UUID.randomUUID();
            var existingItem = QuoteStockItem.reconstruct(existingId, validStock(),
                    new BigDecimal(PRICE_100), 1, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(existingItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 5)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getStockItems()).hasSize(1);
            assertThat(result.getStockItems().getFirst().getId()).isEqualTo(existingId);
            assertThat(result.getStockItems().getFirst().getQuantity()).isEqualTo(5);
        }
    }

    @Nested
    class ServiceItemReconciliation {

        @Test
        void shouldReuseExistingExecutionWhenServiceCatalogUnchanged() {
            var existingItemId = UUID.randomUUID();
            var execution = ServiceOrderExecution.create(SERVICE_CATALOG_ID, SERVICE_ORDER_ID);
            var existingServiceItem = QuoteServiceItem.reconstruct(existingItemId, execution,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithServiceItems(List.of(existingServiceItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(), List.of(new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getServiceItems()).hasSize(1);
            assertThat(result.getServiceItems().getFirst().getId()).isEqualTo(existingItemId);
            verify(serviceOrderExecutionRepository, never()).save(any(ServiceOrderExecution.class));
        }

        @Test
        void shouldCancelExecutionWhenServiceItemIsRemoved() {
            var execution = ServiceOrderExecution.create(SERVICE_CATALOG_ID, SERVICE_ORDER_ID);
            var removedServiceItem = QuoteServiceItem.reconstruct(UUID.randomUUID(), execution,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());

            var keptStockItem = QuoteStockItem.reconstruct(UUID.randomUUID(), validStock(),
                    new BigDecimal(PRICE_100), 2, LocalDateTime.now(), LocalDateTime.now());

            var quote = quoteWithStockAndServiceItems(List.of(keptStockItem), List.of(removedServiceItem));

            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            assertThat(execution.getStatus()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
            verify(serviceOrderExecutionRepository).save(execution);
        }
    }

    private Quote pendingQuote() {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote quoteWithStockItems(List<QuoteStockItem> stockItems) {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, stockItems, List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote quoteWithServiceItems(List<QuoteServiceItem> serviceItems) {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), serviceItems,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Stock validStock() {
        return Stock.reconstruct(STOCK_ID, "Filtro de óleo", "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceCatalog validServiceCatalog() {
        return ServiceCatalog.reconstruct(SERVICE_CATALOG_ID, "Troca de óleo",
                "Troca de óleo do motor", new BigDecimal(PRICE_150),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Stock validStockSecondary() {
        return Stock.reconstruct(STOCK_ID_SECONDARY, "Vela de ignição", "Vela",
                10, new BigDecimal(PRICE_100), "Ignição", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote quoteWithStockAndServiceItems(List<QuoteStockItem> stockItems, List<QuoteServiceItem> serviceItems) {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, stockItems, serviceItems,
                LocalDateTime.now(), LocalDateTime.now());
    }
}