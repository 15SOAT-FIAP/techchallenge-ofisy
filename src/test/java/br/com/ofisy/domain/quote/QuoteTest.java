package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuoteTest {

    private static final String PRICE_100 = "100.00";
    private static final String PRICE_50 = "50.00";
    private static final String PRICE_350 = "350.00";
    private static final String REFUSAL_REASON = "Achei o valor das peças do orçamento muito alto";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar orçamento com status PENDING")
        void shouldCreateQuoteWithPendingStatus() {
            var quote = mockQuote(List.of(mockStockItem(new BigDecimal(PRICE_100), 2)), List.of());

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
            assertThat(quote.getCreatedAt()).isNotNull();
            assertThat(quote.getUpdatedAt()).isNull();
            assertThat(quote.getQuoteRefusalReason()).isNull();
        }

        /* Comentando até termos tudo do serviço
        @Test
        @DisplayName("Deve calcular total somando itens de estoque e serviços")
        void shouldCalculateTotalWithStockAndServiceItems() {
            var stockItems = List.of(
                    mockStockItem(new BigDecimal(PRICE_100), 2),
                    mockStockItem(new BigDecimal(PRICE_50), 3)
            );
            var serviceItems = List.of(mockServiceItem(new BigDecimal(PRICE_100)));
            var quote = mockQuote(stockItems, serviceItems);

            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_450));
        } */

        @Test
        @DisplayName("Deve calcular total apenas com itens de estoque")
        void shouldCalculateTotalWithOnlyStockItems() {
            var stockItems = List.of(
                    mockStockItem(new BigDecimal(PRICE_100), 2),
                    mockStockItem(new BigDecimal(PRICE_50), 3)
            );
            var quote = mockQuote(stockItems, List.of());

            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_350));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas são nulas")
        void shouldThrowExceptionWhenBothListsAreNull() {
            UUID serviceOrderId = UUID.randomUUID();

            assertThatThrownBy(() -> Quote.create(serviceOrderId, null, null))
                    .isInstanceOf(InvalidQuoteDataException.class)
                    .hasMessageContaining(serviceOrderId.toString());
        }

        @Test
        @DisplayName("Deve associar itens de estoque ao orçamento")
        void shouldAssociateStockItemsToQuote() {
            var items = List.of(mockStockItem(new BigDecimal(PRICE_100), 2));
            var quote = mockQuote(items, List.of());

            assertThat(quote.getStockItems()).hasSize(1);
            assertThat(quote.getStockItems().getFirst().getQuote()).isEqualTo(quote);
        }

        /* Comentando aqui até termos tudo do serviço
        @Test
        @DisplayName("Deve associar itens de serviço ao orçamento")
        void shouldAssociateServiceItemsToQuote() {
            var items = List.of(mockServiceItem(new BigDecimal(PRICE_100)));
            var quote = mockQuote(List.of(), items);

            assertThat(quote.getServiceItems()).hasSize(1);
            assertThat(quote.getServiceItems().getFirst().getQuote()).isEqualTo(quote);
        } */
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            var quote = mockQuote(List.of(mockStockItem(new BigDecimal(PRICE_100), 1)), List.of());

            quote.approve();

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(quote.getUpdatedAt()).isNotNull();
            assertThat(quote.getQuoteRefusalReason()).isNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            var quote = mockQuote(List.of(mockStockItem(new BigDecimal(PRICE_100), 1)), List.of());
            quote.approve();

            assertThatThrownBy(quote::approve)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(Quote.ACTION_APPROVE);
        }
    }

    @Nested
    @DisplayName("reprove")
    class Reprove {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            var quote = mockQuote(List.of(mockStockItem(new BigDecimal(PRICE_100), 1)), List.of());

            quote.reprove(REFUSAL_REASON);

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(quote.getQuoteRefusalReason()).isEqualTo(REFUSAL_REASON);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            var quote = mockQuote(List.of(mockStockItem(new BigDecimal(PRICE_100), 1)), List.of());
            quote.reprove(REFUSAL_REASON);

            assertThatThrownBy(() -> quote.reprove(REFUSAL_REASON))
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(Quote.ACTION_REPROVE);
        }
    }

    private Stock mockStock(BigDecimal unitPrice) {
        return Stock.create("Filtro de óleo", "Filtro", 10, unitPrice, "Filtros", 2);
    }


    private QuoteStockItem mockStockItem(BigDecimal unitPrice, Integer quantity) {
        return QuoteStockItem.create(mockStock(unitPrice), quantity);
    }

    private Quote mockQuote(List<QuoteStockItem> stockItems, List<QuoteServiceItem> serviceItems) {
        return Quote.create(UUID.randomUUID(), new ArrayList<>(stockItems), new ArrayList<>(serviceItems));
    }

     /* Comentando aqui até termos a parte de serviços
    private ServiceOrderExecution mockServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    }

    private QuoteServiceItem mockServiceItem(BigDecimal price) {
        return QuoteServiceItem.create(mockServiceOrderExecution(), price);
    }   */

}
