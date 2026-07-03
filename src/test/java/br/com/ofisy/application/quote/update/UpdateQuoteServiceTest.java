package br.com.ofisy.application.quote.update;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.serviceorder.updatequote.UpdateQuoteService;
import br.com.ofisy.application.serviceorder.updatequote.UpdateQuoteUseCase;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuoteServiceTest {

    private static final UUID QUOTE_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();
    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final UUID SERVICE_CATALOG_ID = UUID.randomUUID();
    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";

    @Mock private QuoteRepository quoteRepository;
    @Mock private StockRepository stockRepository;
    @Mock private ServiceCatalogRepository serviceCatalogRepository;
    @Mock private ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Mock private ConsumeStockUseCase consumeStockUseCase;

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

    private Quote pendingQuote() {
        return Quote.reconstruct(QUOTE_ID, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
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
}
