package br.com.ofisy.application.quote.update;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
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
    private static final String PRODUCT_NAME = "Filtro de óleo";
    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";

    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private UpdateQuoteService service;

    @Nested
    class Execute {

        @Test
        void shouldUpdateQuoteWithNewStockItemsSuccessfully() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(stockItemCommand(3)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getStockItems()).hasSize(1);
            assertThat(result.getStockItems().getFirst().getQuantity()).isEqualTo(3);
            assertThat(result.getStockItems().getFirst().getStockId()).isEqualTo(STOCK_ID);
            assertThat(result.getStockItems().getFirst().getProductName()).isEqualTo(PRODUCT_NAME);
            assertThat(result.getStockItems().getFirst().getUnitPrice()).isEqualByComparingTo(PRICE_100);
            verify(quoteRepository).save(quote);
        }

        @Test
        void shouldUpdateQuoteWithNewServiceItemsSuccessfully() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(), List.of(new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getServiceItems()).hasSize(1);
            assertThat(result.getServiceItems().getFirst().getServiceCatalogId()).isEqualTo(SERVICE_CATALOG_ID);
            assertThat(result.getServiceItems().getFirst().getPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }

        @Test
        void shouldRecalculateTotalPriceAfterUpdate() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(stockItemCommand(2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
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
                    List.of(stockItemCommand(1)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }

        @Test
        void shouldThrowWhenNoItemsAreSent() {
            var quote = pendingQuote();
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID, null, null);

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InvalidQuoteItemException.class);
            verify(quoteRepository, never()).save(any());
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
                    List.of(stockItemCommand(1), stockItemCommand(2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class)
                    .hasMessageContaining(PRODUCT_NAME);
        }
    }

    @Nested
    class StockItemReconciliation {

        @Test
        void shouldPreserveStockItemIdAndApplyRequestData() {
            var existingId = UUID.randomUUID();
            var createdAt = LocalDateTime.now().minusDays(1);
            var existingItem = QuoteStockItem.reconstruct(existingId, STOCK_ID, "Nome antigo",
                    new BigDecimal("80.00"), 1, createdAt, createdAt);
            var quote = quoteWithStockItems(List.of(existingItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(stockItemCommand(5)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getStockItems()).hasSize(1);
            var item = result.getStockItems().getFirst();
            assertThat(item.getId()).isEqualTo(existingId);
            assertThat(item.getStockId()).isEqualTo(STOCK_ID);
            assertThat(item.getProductName()).isEqualTo(PRODUCT_NAME);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(PRICE_100);
            assertThat(item.getQuantity()).isEqualTo(5);
            assertThat(item.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        void shouldRemoveStockItemNoLongerRequested() {
            var keptItem = QuoteStockItem.reconstruct(UUID.randomUUID(), STOCK_ID, PRODUCT_NAME,
                    new BigDecimal(PRICE_100), 2, LocalDateTime.now(), LocalDateTime.now());
            var removedItem = QuoteStockItem.reconstruct(UUID.randomUUID(), STOCK_ID_SECONDARY, "Vela de ignição",
                    new BigDecimal(PRICE_100), 4, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(List.of(keptItem, removedItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(stockItemCommand(2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getStockItems())
                    .extracting(QuoteStockItem::getStockId)
                    .containsExactly(STOCK_ID);
        }
    }

    @Nested
    class ServiceItemReconciliation {

        @Test
        void shouldReuseExistingServiceItemWhenServiceCatalogUnchanged() {
            var existingItemId = UUID.randomUUID();
            var existingServiceItem = QuoteServiceItem.reconstruct(existingItemId, SERVICE_CATALOG_ID,
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
            assertThat(result.getServiceItems().getFirst().getServiceCatalogId()).isEqualTo(SERVICE_CATALOG_ID);
        }

        @Test
        void shouldPreserveOriginalPriceWhenServiceCatalogPriceChanges() {
            var originalPrice = new BigDecimal("120.00");
            var existingServiceItem = QuoteServiceItem.reconstruct(UUID.randomUUID(), SERVICE_CATALOG_ID,
                    originalPrice, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithServiceItems(List.of(existingServiceItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(), List.of(new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog()));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getServiceItems().getFirst().getPrice()).isEqualByComparingTo(originalPrice);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(originalPrice);
        }

        @Test
        void shouldRemoveServiceItemNoLongerRequested() {
            var removedServiceItem = QuoteServiceItem.reconstruct(UUID.randomUUID(), SERVICE_CATALOG_ID,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());
            var keptStockItem = QuoteStockItem.reconstruct(UUID.randomUUID(), STOCK_ID, PRODUCT_NAME,
                    new BigDecimal(PRICE_100), 2, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockAndServiceItems(List.of(keptStockItem), List.of(removedServiceItem));
            var cmd = new UpdateQuoteUseCase.UpdateQuoteCommand(QUOTE_ID,
                    List.of(stockItemCommand(2)), List.of());

            when(quoteRepository.findById(QUOTE_ID)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getServiceItems()).isEmpty();
            assertThat(result.getStockItems()).hasSize(1);
        }
    }

    private CreateQuoteUseCase.StockItemCommand stockItemCommand(int quantity) {
        return new CreateQuoteUseCase.StockItemCommand(STOCK_ID, PRODUCT_NAME, new BigDecimal(PRICE_100), quantity);
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

    private Quote quoteWithStockAndServiceItems(List<QuoteStockItem> stockItems, List<QuoteServiceItem> serviceItems) {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, stockItems, serviceItems,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceCatalog validServiceCatalog() {
        return ServiceCatalog.reconstruct(SERVICE_CATALOG_ID, "Troca de óleo",
                "Troca de óleo do motor", new BigDecimal(PRICE_150),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
