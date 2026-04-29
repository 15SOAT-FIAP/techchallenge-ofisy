package br.com.ofisy.application.quote;

import br.com.ofisy.domain.quote.*;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteMapperTest {

    public static final String PRICE_100 = "100.00";
    public static final String PRICE_200 = "200.00";
    public static final String PRICE_150 = "150.00";
    public static final String STOCK_PRODUCT_NAME = "Filtro de óleo";

    private final QuoteMapper mapper = new QuoteMapper();

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("Deve mapear Quote para QuoteResponseDTO corretamente")
        void shouldMapQuoteToResponseDTO() {
            var quote = mockQuote();

            var response = mapper.toResponse(quote);

            assertThat(response.serviceOrderId()).isEqualTo(quote.getServiceOrderId());
            assertThat(response.status()).isEqualTo(quote.getStatus());
            assertThat(response.totalPrice()).isEqualByComparingTo(quote.getTotalPrice());
            assertThat(response.quoteRefusalReason()).isNull();
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve mapear itens de estoque corretamente")
        void shouldMapStockItemsCorrectly() {
            var quote = mockQuote();

            var response = mapper.toResponse(quote);

            assertThat(response.stockItems()).hasSize(1);
            assertThat(response.stockItems().getFirst().productName()).isEqualTo(STOCK_PRODUCT_NAME);
            assertThat(response.stockItems().getFirst().quantity()).isEqualTo(2);
            assertThat(response.stockItems().getFirst().unitPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(response.stockItems().getFirst().subtotal()).isEqualByComparingTo(new BigDecimal(PRICE_200));
            assertThat(response.stockItems().getFirst().createdAt()).isNotNull();
        }

        /* Comentando até termos os itens de serviço
        @Test
        @DisplayName("Deve mapear itens de serviço corretamente")
        void shouldMapServiceItemsCorrectly() {
            var quote = mockQuote();

            var response = mapper.toResponse(quote);

            assertThat(response.serviceItems()).hasSize(1);
            assertThat(response.serviceItems().getFirst().price()).isEqualByComparingTo(new BigDecimal(PRICE_150));
        }
        */
        
        @Test
        @DisplayName("Deve mapear motivo de reprovação quando presente")
        void shouldMapRefusalReasonWhenPresent() {
            var quote = mockQuote();
            quote.reprove("Achei o valor do serviço muito alto.");

            var response = mapper.toResponse(quote);

            assertThat(response.quoteRefusalReason()).isEqualTo("Achei o valor do serviço muito alto.");
            assertThat(response.status()).isEqualTo(QuoteStatus.REPROVED);
        }
    }

    private Stock mockStock() {
        return Stock.create(STOCK_PRODUCT_NAME, "Filtro", 10, new BigDecimal(PRICE_100), "Filtros", 2);
    }

    /* Comentando esse ponto até termos os serviços
    private ServiceOrderExecution mockServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    } */

    private Quote mockQuote() {
        Stock stock = mockStock();
        List<QuoteStockItem> stockItems = List.of(QuoteStockItem.create(stock, 2));
        /* Comentando esse ponto até termos os serviços
            List<QuoteServiceItem> serviceItems = List.of(QuoteServiceItem.create(mockServiceOrderExecution(), new BigDecimal(PRICE_150)));
            return Quote.create(UUID.randomUUID(), stockItems, serviceItems);
        */
        return Quote.create(UUID.randomUUID(), stockItems, new ArrayList<>());
    }
}
