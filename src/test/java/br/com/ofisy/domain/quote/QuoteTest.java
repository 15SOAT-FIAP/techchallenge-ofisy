package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuoteTest {

    public static final String PRICE_100 = "100.00";
    public static final String PRICE_50 = "50.00";
    public static final String PRICE_350 = "350.00";
    public static final String ACTION_REPROVE = "reprovar";
    public static final String ACTION_REPROVE_PARTIALLY = "reprovar parcialmente";
    public static final String ACTION_APPROVE = "aprovar";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar orçamento com status PENDING")
        void shouldCreateQuoteWithPendingStatus() {
            var items = List.of(mockItem(new BigDecimal(PRICE_100), 2));
            var quote = mockQuote(items);

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
            assertThat(quote.getCreatedAt()).isNotNull();
            assertThat(quote.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Deve calcular total corretamente")
        void shouldCalculateTotalCorrectly() {
            var items = List.of(
                    mockItem(new BigDecimal(PRICE_100), 2),
                    mockItem(new BigDecimal(PRICE_50), 3)
            );
            var quote = mockQuote(items);

            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_350));
        }

        @Test
        @DisplayName("Deve associar itens ao orçamento")
        void shouldAssociateItemsToQuote() {
            var items = List.of(mockItem(new BigDecimal(PRICE_100), 2));
            var quote = mockQuote(items);

            assertThat(quote.getStockItems()).hasSize(1);
            assertThat(quote.getStockItems().getFirst().getQuote()).isEqualTo(quote);
        }

        @Test
        @DisplayName("Deve criar orçamento com total zero quando lista de itens vazia")
        void shouldCreateQuoteWithZeroTotalWhenNoItems() {
            var quote = mockQuote(List.of());

            assertThat(quote.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));

            quote.approve();

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));
            quote.approve();

            assertThatThrownBy(quote::approve)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(ACTION_APPROVE);
        }
    }

    @Nested
    @DisplayName("reprove")
    class Reprove {

        @Test
        @DisplayName("Deve reprovar orçamento pendente")
        void shouldReproveQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));

            quote.reprove();

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));
            quote.reprove();

            assertThatThrownBy(quote::reprove)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(ACTION_REPROVE);
        }
    }

    @Nested
    @DisplayName("reprovePartially")
    class ReprovePartially {

        @Test
        @DisplayName("Deve reprovar parcialmente orçamento pendente")
        void shouldReprovePartiallyQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));

            quote.reprovePartially();

            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PARTIALLY_REPROVED);
            assertThat(quote.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar parcialmente orçamento não pendente")
        void shouldThrowExceptionWhenReprovePartiallyNonPendingQuote() {
            var quote = mockQuote(List.of(mockItem(new BigDecimal(PRICE_100), 1)));
            quote.approve();

            assertThatThrownBy(quote::reprovePartially)
                    .isInstanceOf(InvalidQuoteStatusException.class)
                    .hasMessageContaining(ACTION_REPROVE_PARTIALLY);
        }
    }

    private Stock mockStock(BigDecimal unitPrice) {
        return Stock.create("Filtro de óleo", "Filtro", 10, unitPrice, "Filtros", 2);
    }

    private QuoteStockItem mockItem(BigDecimal unitPrice, Integer quantity) {
        return QuoteStockItem.create(mockStock(unitPrice), quantity);
    }

    private Quote mockQuote(List<QuoteStockItem> items) {
        return Quote.create(UUID.randomUUID(), items);
    }
}
