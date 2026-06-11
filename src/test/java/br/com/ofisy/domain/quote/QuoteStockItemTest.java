package br.com.ofisy.domain.quote;

import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuoteStockItemTest {

    public static final String PRICE_150 = "150.00";
    public static final String PRICE_100 = "100.00";
    public static final String PRICE_1000 = "1000.0";
    public static final String INVALID_STOCK_ITEM_QTY_MSG = "Quantidade do item deve ser maior que zero!";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item do snapshot com do preço atual do item")
        void shouldCreateItemWithStockPriceSnapshot() {
            var stock = mockStockItem1(new BigDecimal(PRICE_150));
            var item = QuoteStockItem.create(stock, 3);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
            assertThat(item.getQuantity()).isEqualTo(3);
            // TODO: Validação do estoque temporariamente removida durante a migração
            // do contexto de Orçamento para Clean Architecture. Atualmente o item
            // armazena StockEntity como solução provisória até a criação de
            // QuoteEntity/QuoteStockItemEntity.
            assertThat(item.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve manter informações do snapshot mesmo que preço do item mude")
        void shouldMaintainSnapshotEvenIfStockPriceChanges() {
            var stock = mockStockItem1(new BigDecimal(PRICE_150));
            var item = QuoteStockItem.create(stock, 1);

            stock.update(null, null, new BigDecimal("200.00"), null, null);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }
    }

    @Nested
    @DisplayName("setQuote")
    class SetQuote {

        @Test
        @DisplayName("Deve associar quote a um item")
        void shouldSetQuoteToItem() {
            var stock = mockStockItem1(new BigDecimal(PRICE_100));
            var item = QuoteStockItem.create(stock, 2);
            var quote = Quote.create(UUID.randomUUID(), List.of(item), List.of());

            item.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }

        @Test
        @DisplayName("Deve associar quote a mais de um item")
        void shouldSetQuoteToTwoItems() {
            var stock = mockStockItem1(new BigDecimal(PRICE_100));
            var stock2 = mockStockItem2(new BigDecimal(PRICE_1000));
            var item = QuoteStockItem.create(stock, 2);
            var item2 = QuoteStockItem.create(stock2, 4);
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
            var stock = mockStockItem1(new BigDecimal(PRICE_100));

            assertThatThrownBy(() -> QuoteStockItem.create(stock, 0))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade é nula")
        void shouldThrowExceptionWhenQuantityIsNull() {
            var stock = mockStockItem1(new BigDecimal(PRICE_100));

            assertThatThrownBy(() -> QuoteStockItem.create(stock, null))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade é negativa")
        void shouldThrowExceptionWhenQuantityIsNegative() {
            var stock = mockStockItem1(new BigDecimal(PRICE_100));

            assertThatThrownBy(() -> QuoteStockItem.create(stock, -1))
                    .isInstanceOf(InvalidQuoteItemException.class)
                    .hasMessageContaining(INVALID_STOCK_ITEM_QTY_MSG);
        }
    }

    private Stock mockStockItem1(BigDecimal unitPrice) {
        return Stock.create("Filtro de óleo", "Filtro", 10, unitPrice, "Filtros", 2);
    }

    private Stock mockStockItem2(BigDecimal unitPrice) {
        return Stock.create("Pneu Firestone", "Pneu Firestone", 4, unitPrice, "Pneu", 4);
    }
}