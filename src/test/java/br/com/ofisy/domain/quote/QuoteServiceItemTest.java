package br.com.ofisy.domain.quote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class QuoteServiceItemTest {

    private static final String PRICE_100 = "100.00";
    private static final String PRICE_200 = "200.00";

    /* Comentando até termos tudo dos serviços
    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item com snapshot do preço do serviço")
        void shouldCreateItemWithServicePriceSnapshot() {
            var serviceOrderExecution = mockServiceOrderExecution();
            var item = QuoteServiceItem.create(serviceOrderExecution, new BigDecimal(PRICE_100));

            assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(item.getServiceOrderExecution()).isEqualTo(serviceOrderExecution);
            assertThat(item.getQuote()).isNull();
        }

        @Test
        @DisplayName("Deve manter snapshot mesmo que preço do serviço mude")
        void shouldMaintainSnapshotEvenIfServicePriceChanges() {
            var serviceOrderExecution = mockServiceOrderExecution();
            var item = QuoteServiceItem.create(serviceOrderExecution, new BigDecimal(PRICE_100));

            assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
        }
    }

    @Nested
    @DisplayName("setQuote")
    class SetQuote {

        @Test
        @DisplayName("Deve associar quote ao item")
        void shouldSetQuoteToItem() {
            var item = QuoteServiceItem.create(mockServiceOrderExecution(), new BigDecimal(PRICE_100));
            var quote = Quote.create(UUID.randomUUID(), List.of(), List.of(item));

            item.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }
    }


    private ServiceOrderExecution mockServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    } */
}
