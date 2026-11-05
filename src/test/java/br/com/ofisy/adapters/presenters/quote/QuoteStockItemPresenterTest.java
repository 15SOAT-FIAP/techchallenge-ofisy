package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.dto.QuoteStockItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteStockItem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
            QuoteStockItem item = validItem(stockId, 3);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.stockId()).isEqualTo(stockId);
            assertThat(dto.productName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(dto.unitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(dto.quantity()).isEqualTo(3);
        }

        @Test
        void shouldCalculateSubtotalCorrectly() {
            QuoteStockItem item = validItem(UUID.randomUUID(), 4);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.subtotal()).isEqualByComparingTo(new BigDecimal(PRICE_400));
        }

        @Test
        void shouldSetCreatedAtFromItem() {
            QuoteStockItem item = validItem(UUID.randomUUID(), 1);

            QuoteStockItemResponseDTO dto = QuoteStockItemPresenter.present(item);

            assertThat(dto.createdAt()).isEqualTo(item.getCreatedAt());
        }
    }

    private QuoteStockItem validItem(UUID stockId, int quantity) {
        return QuoteStockItem.create(stockId, FILTRO_DE_OLEO, new BigDecimal(PRICE_100), quantity);
    }
}
