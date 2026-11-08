package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuoteStockItemTest {

    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";
    public static final String PRICE_1000 = "1000.0";
    public static final String FILTRO_DE_OLEO = "Filtro de óleo";
    public static final String PNEU_FIRESTONE = "Pneu Firestone";
    public static final String INVALID_STOCK_ITEM_QTY_MSG = "Quantidade do item deve ser maior que zero!";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item com a referência do estoque, o nome e o preço informados")
        void shouldCreateItemWithStockReferenceAndSnapshot() {
            var stockId = UUID.randomUUID();
            var item = QuoteStockItem.create(stockId, FILTRO_DE_OLEO, new BigDecimal(PRICE_150), 3);

            assertThat(item.getStockId()).isEqualTo(stockId);
            assertThat(item.getProductName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getCreatedAt()).isNotNull();
            assertThat(item.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("Deve reconstruir item com todos os campos persistidos")
        void shouldReconstructItemWithAllFields() {
            var id = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

            var item = QuoteStockItem.reconstruct(id, stockId, FILTRO_DE_OLEO,
                    new BigDecimal(PRICE_100), 2, createdAt, updatedAt);

            assertThat(item.getId()).isEqualTo(id);
            assertThat(item.getStockId()).isEqualTo(stockId);
            assertThat(item.getProductName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getCreatedAt()).isEqualTo(createdAt);
            assertThat(item.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("setQuote")
    class SetQuote {

        @Test
        @DisplayName("Deve associar quote a um item")
        void shouldSetQuoteToItem() {
            var item = mockStockItem1(new BigDecimal(PRICE_100), 2);
            var quote = Quote.create(UUID.randomUUID(), List.of(item), List.of());

            item.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }

        @Test
        @DisplayName("Deve associar quote a mais de um item")
        void shouldSetQuoteToTwoItems() {
            var item = mockStockItem1(new BigDecimal(PRICE_100), 2);
            var item2 = mockStockItem2(new BigDecimal(PRICE_1000), 4);
            var quote = Quote.create(UUID.randomUUID(), List.of(item, item2), List.of());

            item.setQuote(quote);
            item2.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }
    }

    @Nested
    @DisplayName("invalidItem")
    class InvalidItem {

        @Test
        @DisplayName("Deve lançar exceção quando quantidade é zero")
        void shouldThrowExceptionWhenQuantityIsZero() {
            assertThatThrownBy(() -> mockStockItem1(new BigDecimal(PRICE_100), 0))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade é nula")
        void shouldThrowExceptionWhenQuantityIsNull() {
            assertThatThrownBy(() -> mockStockItem1(new BigDecimal(PRICE_100), null))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade é negativa")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            assertThatThrownBy(() -> mockStockItem1(new BigDecimal(PRICE_100), -1))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }
    }

    private QuoteStockItem mockStockItem1(BigDecimal unitPrice, Integer quantity) {
        return QuoteStockItem.create(UUID.randomUUID(), FILTRO_DE_OLEO, unitPrice, quantity);
    }

    private QuoteStockItem mockStockItem2(BigDecimal unitPrice, Integer quantity) {
        return QuoteStockItem.create(UUID.randomUUID(), PNEU_FIRESTONE, unitPrice, quantity);
    }
}
