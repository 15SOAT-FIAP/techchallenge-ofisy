package br.com.ofisy.domain.quote;

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

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item do snapshot com do preço atual do item")
        void shouldCreateItemWithStockPriceSnapshot() {
            var stock = mockStock(new BigDecimal(PRICE_150));
            var item = QuoteStockItem.create(stock, 3);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getStock()).isEqualTo(stock);
        }

        @Test
        @DisplayName("Deve manter informações do snapshot mesmo que preço do item mude")
        void shouldMaintainSnapshotEvenIfStockPriceChanges() {
            var stock = mockStock(new BigDecimal(PRICE_150));
            var item = QuoteStockItem.create(stock, 1);

            stock.update(null, null, new BigDecimal("200.00"), null, null);

            assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }

        @Test
        @DisplayName("Deve criar item sem quote associado inicialmente")
        void shouldCreateItemWithoutQuoteInitially() {
            var stock = mockStock(new BigDecimal(PRICE_100));
            var item = QuoteStockItem.create(stock, 2);

            assertThat(item.getQuote()).isNull();
        }
    }

    @Nested
    @DisplayName("setQuote")
    class SetQuote {

        @Test
        @DisplayName("Deve associar quote ao item")
        void shouldSetQuoteToItem() {
            var stock = mockStock(new BigDecimal(PRICE_100));
            var item = QuoteStockItem.create(stock, 2);
            var quote = Quote.create(UUID.randomUUID(), List.of());

            item.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }
    }

    private Stock mockStock(BigDecimal unitPrice) {
        return Stock.create("Filtro de óleo", "Filtro", 10, unitPrice, "Filtros", 2);
    }
}