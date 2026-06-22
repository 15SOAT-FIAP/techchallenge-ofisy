package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.dto.QuoteResponseDTO;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuotePresenterTest {

    public static final String PRICE_200 = "200.00";
    public static final String PRICE_100 = "100.00";

    @Nested
    class Present {

        @Test
        void shouldMapAllFieldsCorrectly() {
            UUID id = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            Quote quote = Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                    new BigDecimal(PRICE_100), null, List.of(), List.of(),
                    LocalDateTime.now(), LocalDateTime.now());

            QuoteResponseDTO dto = QuotePresenter.present(quote);

            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo(id);
            assertThat(dto.serviceOrderId()).isEqualTo(serviceOrderId);
            assertThat(dto.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_100));
            assertThat(dto.quoteRefusalReason()).isNull();
            assertThat(dto.stockItems()).isEmpty();
            assertThat(dto.serviceItems()).isEmpty();
        }

        @Test
        void shouldSetTimestampsFromQuote() {
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            Quote quote = Quote.reconstruct(UUID.randomUUID(), UUID.randomUUID(), QuoteStatus.PENDING,
                    new BigDecimal(PRICE_100), null, List.of(), List.of(), createdAt, updatedAt);

            QuoteResponseDTO dto = QuotePresenter.present(quote);

            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldMapApprovedQuoteCorrectly() {
            Quote quote = Quote.reconstruct(UUID.randomUUID(), UUID.randomUUID(), QuoteStatus.APPROVED,
                    new BigDecimal(PRICE_200), null, List.of(), List.of(),
                    LocalDateTime.now(), LocalDateTime.now());

            QuoteResponseDTO dto = QuotePresenter.present(quote);

            assertThat(dto.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(dto.quoteRefusalReason()).isNull();
        }

        @Test
        void shouldMapReprovadQuoteWithReason() {
            String reason = "Preço muito alto";
            Quote quote = Quote.reconstruct(UUID.randomUUID(), UUID.randomUUID(), QuoteStatus.REPROVED,
                    new BigDecimal(PRICE_200), reason, List.of(), List.of(),
                    LocalDateTime.now(), LocalDateTime.now());

            QuoteResponseDTO dto = QuotePresenter.present(quote);

            assertThat(dto.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(dto.quoteRefusalReason()).isEqualTo(reason);
        }

        @Test
        void shouldHaveNullIdForNewQuote() {
            Quote quote = Quote.reconstruct(null, UUID.randomUUID(), QuoteStatus.PENDING,
                    new BigDecimal(PRICE_100), null, List.of(), List.of(),
                    LocalDateTime.now(), LocalDateTime.now());

            QuoteResponseDTO dto = QuotePresenter.present(quote);

            assertThat(dto.id()).isNull();
        }
    }
}
