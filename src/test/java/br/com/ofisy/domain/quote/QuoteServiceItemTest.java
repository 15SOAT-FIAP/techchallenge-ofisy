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

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item com snapshot do preço do serviço")
        void shouldCreateItemWithServicePriceSnapshot() {
            var serviceCatalogId = UUID.randomUUID();
            var item = QuoteServiceItem.create(serviceCatalogId, new BigDecimal(PRICE_100));

            assertThat(item.getPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(item.getServiceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(item.getQuote()).isNull();
            assertThat(item.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("setQuote")
    class SetQuote {

        @Test
        @DisplayName("Deve criar quote com um serviço")
        void shouldAssociateQuoteWithOneItem() {
            var item = QuoteServiceItem.create(UUID.randomUUID(), new BigDecimal(PRICE_100));
            var quote = Quote.create(UUID.randomUUID(), List.of(), List.of(item));

            item.setQuote(quote);

            assertThat(item.getQuote()).isEqualTo(quote);
        }

        @Test
        @DisplayName("Deve associar mais de um serviço a um quote")
        void shouldAssociateQuoteWithTwoItems() {
            var item1 = QuoteServiceItem.create(UUID.randomUUID(), new BigDecimal(PRICE_100));
            var item2 = QuoteServiceItem.create(UUID.randomUUID(), new BigDecimal(PRICE_200));
            var quote = Quote.create(UUID.randomUUID(), List.of(), List.of(item1, item2));

            item1.setQuote(quote);
            item2.setQuote(quote);

            assertThat(item1.getQuote()).isEqualTo(quote);
            assertThat(item2.getQuote()).isEqualTo(quote);
        }
    }
}