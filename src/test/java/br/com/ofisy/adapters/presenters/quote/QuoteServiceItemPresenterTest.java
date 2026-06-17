package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.QuoteServiceItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
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
            ServiceOrderExecution execution = ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
            QuoteServiceItem item = QuoteServiceItem.create(execution, new BigDecimal(PRICE_150));

            QuoteServiceItemResponseDTO dto = QuoteServiceItemPresenter.present(item);

            assertThat(dto.serviceOrderExecutionId()).isEqualTo(execution.getId());
            assertThat(dto.price()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }

        @Test
        void shouldSetCreatedAtFromItem() {
            ServiceOrderExecution execution = ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
            QuoteServiceItem item = QuoteServiceItem.create(execution, new BigDecimal(PRICE_150));

            QuoteServiceItemResponseDTO dto = QuoteServiceItemPresenter.present(item);

            assertThat(dto.createdAt()).isEqualTo(item.getCreatedAt());
        }
    }
}
