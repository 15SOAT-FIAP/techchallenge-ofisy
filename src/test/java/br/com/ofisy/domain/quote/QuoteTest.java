package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteTest {

    public static final UUID SERVICE_ORDER_ID = UUID.randomUUID();
    public static final String REPROVE = "reprovar";
    public static final String REPROVED = "REPROVED";
    public static final String APPROVAL = "aprovar";
    public static final String REPROVAL_REASON = "Muito caro";
    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";
    public static final String PRICE_350 = "350.00";

    @Nested
    class Create {

        @Test
        void shouldCreateQuoteWithStockItems() {
            var stockItems = List.of(validStockItem());

            var quote = Quote.create(SERVICE_ORDER_ID, stockItems, List.of());

            assertThat(quote.getServiceOrderId()).isEqualTo(SERVICE_ORDER_ID);
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
            assertThat(quote.getStockItems()).hasSize(1);
            assertThat(quote.getCreatedAt()).isNotNull();
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldCreateQuoteWithServiceItems() {
            var serviceItems = List.of(validServiceItem());

            var quote = Quote.create(SERVICE_ORDER_ID, List.of(), serviceItems);

            assertThat(quote.getServiceItems()).hasSize(1);
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
        }

        @Test
        void shouldCalculateTotalPriceCorrectly() {
            var stockItem = validStockItem();
            var serviceItem = validServiceItem();

            var quote = Quote.create(SERVICE_ORDER_ID, List.of(stockItem), List.of(serviceItem));

            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_350));
        }

        @Test
        void shouldThrowWhenBothItemListsAreEmpty() {
            assertThatThrownBy(() -> Quote.create(SERVICE_ORDER_ID, List.of(), List.of()))
                    .isInstanceOf(InvalidQuoteDataException.class)
                    .hasMessageContaining(SERVICE_ORDER_ID.toString());
        }

        @Test
        void shouldThrowWhenItemListsAreNull() {
            assertThatThrownBy(() -> Quote.create(SERVICE_ORDER_ID, null, null))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }
    }

    @Nested
    class Reconstruct {

        @Test
        void shouldReconstructQuoteWithAllFields() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

            var quote = Quote.reconstruct(id, SERVICE_ORDER_ID, QuoteStatus.PENDING,
                    new BigDecimal(PRICE_100), null, List.of(), List.of(), createdAt, updatedAt);

            assertThat(quote.getId()).isEqualTo(id);
            assertThat(quote.getServiceOrderId()).isEqualTo(SERVICE_ORDER_ID);
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(quote.getCreatedAt()).isEqualTo(createdAt);
            assertThat(quote.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class Approve {

        public static final String APPROVED = "APPROVED";

        @Test
        void shouldApproveWhenStatusIsPending() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());

            quote.approve();

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenApprovingAlreadyApprovedQuote() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());
            quote.approve();

            assertThatThrownBy(quote::approve)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(APPROVAL)
                    .hasMessageContaining(APPROVED);
        }

        @Test
        void shouldThrowWhenApprovingReprovedQuote() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());
            quote.reprove(REPROVAL_REASON);

            assertThatThrownBy(quote::approve)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(APPROVAL);
        }
    }

    @Nested
    class Reprove {

        @Test
        void shouldReproveWhenStatusIsPending() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());

            quote.reprove(REPROVAL_REASON);

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(quote.getQuoteRefusalReason()).isEqualTo(REPROVAL_REASON);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenReprovingAlreadyReprovadQuote() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());
            quote.reprove(REPROVAL_REASON);

            assertThatThrownBy(() -> quote.reprove("Outro motivo"))
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(REPROVE)
                    .hasMessageContaining(REPROVED);
        }

        @Test
        void shouldThrowWhenReprovingApprovedQuote() {
            var quote = Quote.create(SERVICE_ORDER_ID, List.of(validStockItem()), List.of());
            quote.approve();

            assertThatThrownBy(() -> quote.reprove("Motivo"))
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(REPROVE);
        }
    }


    private QuoteServiceItem validServiceItem() {
        ServiceOrderExecution execution = mockServiceOrderExecution();
        return QuoteServiceItem.create(execution, new BigDecimal(PRICE_150));
    }

    private QuoteStockItem validStockItem() {
        var stock = mockStock(new BigDecimal(PRICE_100));
        return QuoteStockItem.create(stock, 2);
    }

    private Stock mockStock(BigDecimal unitPrice) {
        return Stock.create("Filtro de óleo", "Filtro", 10, unitPrice, "Filtros", 2);
    }

    private ServiceOrderExecution mockServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    }

}