package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.dto.QuoteServiceItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceItemPresenterTest {

    public static final String PRICE_150 = "150.00";

    @Nested
    class Present {

        @Test
        void shouldMapAllFieldsCorrectly() {
            UUID serviceCatalogId = UUID.randomUUID();
            QuoteServiceItem item = QuoteServiceItem.create(serviceCatalogId, new BigDecimal(PRICE_150));

            QuoteServiceItemResponseDTO dto = QuoteServiceItemPresenter.present(item);

            assertThat(dto.serviceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(dto.price()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }

        @Test
        void shouldSetCreatedAtFromItem() {
            QuoteServiceItem item = QuoteServiceItem.create(UUID.randomUUID(), new BigDecimal(PRICE_150));

            QuoteServiceItemResponseDTO dto = QuoteServiceItemPresenter.present(item);

            assertThat(dto.createdAt()).isEqualTo(item.getCreatedAt());
        }
    }
}