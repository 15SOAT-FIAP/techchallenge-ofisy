package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteMapperTest {

    public static final String PRICE_100 = "100.00";
    public static final String PRICE_200 = "200.00";
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ServiceOrderExecutionRepository executionRepository;

    @Nested
    class ToEntity {

        @Test
        void shouldMapQuoteFieldsToEntity() {
            var quote = validQuote();

            var entity = QuoteMapper.toEntity(quote);

            assertThat(entity.getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
            assertThat(entity.getStatus()).isEqualTo(quote.getStatus());
            assertThat(entity.getTotalPrice()).isEqualByComparingTo(quote.getTotalPrice());
        }

        @Test
        void shouldMapEmptyItemListsWhenQuoteHasNoItems() {
            var quote = validQuote();

            var entity = QuoteMapper.toEntity(quote);

            assertThat(entity.getStockItems()).isEmpty();
            assertThat(entity.getServiceItems()).isEmpty();
        }

        @Test
        void shouldPreserveTimestamps() {
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var quote = Quote.reconstruct(null, UUID.randomUUID(), QuoteStatus.PENDING,
                    new BigDecimal(PRICE_100), null, List.of(), List.of(), createdAt, updatedAt);

            var entity = QuoteMapper.toEntity(quote);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapQuoteFieldsToDomain() {
            var entity = validEntity();

            var quote = QuoteMapper.toDomain(entity, stockRepository, executionRepository);

            assertThat(quote.getId()).isEqualTo(entity.getId());
            assertThat(quote.getServiceOrderId()).isEqualTo(entity.getServiceOrderId());
            assertThat(quote.getStatus()).isEqualTo(entity.getStatus());
            assertThat(quote.getTotalPrice()).isEqualByComparingTo(entity.getTotalPrice());
        }

        @Test
        void shouldMapStockItemsUsingStockRepository() {
            var stockId = UUID.randomUUID();
            var stock = validStock(stockId);
            var stockItemEntity = QuoteStockItemEntity.builder()
                    .id(UUID.randomUUID())
                    .stockId(stockId)
                    .unitPrice(new BigDecimal(PRICE_100))
                    .quantity(2)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            var entity = QuoteEntity.builder()
                    .id(UUID.randomUUID())
                    .serviceOrderId(UUID.randomUUID())
                    .status(QuoteStatus.PENDING)
                    .totalPrice(new BigDecimal(PRICE_200))
                    .stockItems(List.of(stockItemEntity))
                    .serviceItems(List.of())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

            var quote = QuoteMapper.toDomain(entity, stockRepository, executionRepository);

            assertThat(quote.getStockItems()).hasSize(1);
            assertThat(quote.getStockItems().getFirst().getStock().getId()).isEqualTo(stockId);
        }

        @Test
        void shouldPreserveTimestamps() {
            var entity = validEntity();

            var quote = QuoteMapper.toDomain(entity, stockRepository, executionRepository);

            assertThat(quote.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(quote.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    private Quote validQuote() {
        return Quote.reconstruct(null, UUID.randomUUID(), QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private QuoteEntity validEntity() {
        return QuoteEntity.builder()
                .id(UUID.randomUUID())
                .serviceOrderId(UUID.randomUUID())
                .status(QuoteStatus.PENDING)
                .totalPrice(new BigDecimal(PRICE_100))
                .stockItems(List.of())
                .serviceItems(List.of())
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }

    private Stock validStock(UUID id) {
        return Stock.reconstruct(id, "Filtro de óleo", "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
