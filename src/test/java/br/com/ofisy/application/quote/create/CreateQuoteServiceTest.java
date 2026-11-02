package br.com.ofisy.application.quote.create;

import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
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
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateQuoteServiceTest {

    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();
    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final UUID SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final UUID SERVICE_CATALOG_ID_SECONDARY = UUID.randomUUID();
    public static final String PRICE_100 = "100.00";
    public static final String PRICE_150 = "150.00";

    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;
    @Mock
    private ConsumeStockUseCase consumeStockUseCase;

    @InjectMocks
    private CreateQuoteService service;

    @Nested
    class Execute {

        @Test
        void shouldCreateQuoteWithStockItemsSuccessfully() {
            Stock stock = validStock();
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)), List.of());

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(stock));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            Quote result = service.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getServiceOrderId()).isEqualTo(SERVICE_ORDER_ID);
            assertThat(result.getStockItems()).hasSize(1);
            verify(consumeStockUseCase).execute(new ConsumeStockUseCase.ConsumeStockCommand(STOCK_ID, 2));
            verify(quoteRepository).save(any(Quote.class));
        }

        @Test
        void shouldThrowWhenQuoteAlreadyExistsForServiceOrder() {
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1)), List.of());

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteAlreadyExistsException.class)
                    .hasMessageContaining(SERVICE_ORDER_ID.toString());

            verify(quoteRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenStockNotFound() {
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1)), List.of());

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(StockNotFoundException.class)
                    .hasMessageContaining(STOCK_ID.toString());
        }

        @Test
        void shouldThrowWhenDuplicateStockItem() {
            Stock stock = validStock();
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(
                            new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 1),
                            new CreateQuoteUseCase.StockItemCommand(STOCK_ID, 2)
                    ), List.of());

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }

        @Test
        void shouldCreateQuoteWithServiceItemsUsingCatalogPrice() {
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(), List.of(
                            new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID),
                            new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID_SECONDARY)));

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog(SERVICE_CATALOG_ID, PRICE_150)));
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID_SECONDARY))
                    .thenReturn(Optional.of(validServiceCatalog(SERVICE_CATALOG_ID_SECONDARY, PRICE_100)));
            when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

            Quote result = service.execute(cmd);

            assertThat(result.getServiceItems())
                    .extracting(QuoteServiceItem::getServiceCatalogId, QuoteServiceItem::getPrice)
                    .containsExactly(
                            tuple(SERVICE_CATALOG_ID, new BigDecimal(PRICE_150)),
                            tuple(SERVICE_CATALOG_ID_SECONDARY, new BigDecimal(PRICE_100)));
            verify(serviceOrderExecutionRepository, times(2)).save(any(ServiceOrderExecution.class));
        }

        @Test
        void shouldThrowWhenDuplicateServiceItem() {
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID,
                    List.of(), List.of(
                            new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID),
                            new CreateQuoteUseCase.ServiceItemCommand(SERVICE_CATALOG_ID)));

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);
            when(serviceCatalogRepository.findById(SERVICE_CATALOG_ID))
                    .thenReturn(Optional.of(validServiceCatalog(SERVICE_CATALOG_ID, PRICE_150)));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);

            verify(serviceOrderExecutionRepository, times(1)).save(any(ServiceOrderExecution.class));
            verify(quoteRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenBothListsAreNullAndResultInEmptyQuote() {
            CreateQuoteUseCase.CreateQuoteCommand cmd = new CreateQuoteUseCase.CreateQuoteCommand(SERVICE_ORDER_ID, null, null);

            when(quoteRepository.existsByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }
    }

    private Stock validStock() {
        return Stock.reconstruct(CreateQuoteServiceTest.STOCK_ID, "Filtro de óleo", "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceCatalog validServiceCatalog(UUID id, String price) {
        return ServiceCatalog.reconstruct(id, "Serviço " + id, "Descrição do serviço",
                new BigDecimal(price), LocalDateTime.now(), LocalDateTime.now());
    }
}
