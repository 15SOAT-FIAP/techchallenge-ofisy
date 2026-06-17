package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.QuoteStockItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteStockItemPresenterTest {

    public static final String PRICE_100 = "100.00";
    public static final String PRICE_400 = "400.00";
    public static final String FILTRO_DE_OLEO = "Filtro de óleo";

    @Nested
    class Present {

        @Test
        void shouldMapAllFieldsCorrectly() {
            UUID stockId = UUID.randomUUID();
            Stock stock = validStock(stockId);
            QuoteStockItem item = QuoteStockItem.create(stock, 3);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.stockId()).isEqualTo(stockId);
            assertThat(dto.productName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(dto.unitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(dto.quantity()).isEqualTo(3);
        }

        @Test
        void shouldCalculateSubtotalCorrectly() {
            Stock stock = validStock(UUID.randomUUID());
            QuoteStockItem item = QuoteStockItem.create(stock, 4);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.subtotal()).isEqualByComparingTo(new BigDecimal(PRICE_400));
        }

        @Test
        void shouldSetCreatedAtFromItem() {
            Stock stock = validStock(UUID.randomUUID());
            QuoteStockItem item = QuoteStockItem.create(stock, 1);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.createdAt()).isEqualTo(item.getCreatedAt());
        }
    }

    private Stock validStock(UUID id) {
        return Stock.reconstruct(id, FILTRO_DE_OLEO, "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
