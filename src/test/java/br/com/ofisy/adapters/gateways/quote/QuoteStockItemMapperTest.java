package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteStockItem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteStockItemMapperTest {

    public static final String PRICE_100 = "100.00";
    public static final String FILTRO_DE_OLEO = "Filtro de óleo";

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var stockId = UUID.randomUUID();
            var entity = validEntity(stockId);

            var item = QuoteStockItemMapper.toDomain(entity);

            assertThat(item.getId()).isEqualTo(entity.getId());
            assertThat(item.getStockId()).isEqualTo(stockId);
            assertThat(item.getProductName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(entity.getUnitPrice());
            assertThat(item.getQuantity()).isEqualTo(entity.getQuantity());
        }

        @Test
        void shouldPreserveTimestamps() {
            var entity = validEntity(UUID.randomUUID());

            var item = QuoteStockItemMapper.toDomain(entity);

            assertThat(item.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(item.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var stockId = UUID.randomUUID();
            var item = QuoteStockItem.create(stockId, FILTRO_DE_OLEO, new BigDecimal(PRICE_100), 3);
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteStockItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getStockId()).isEqualTo(stockId);
            assertThat(entity.getProductName()).isEqualTo(FILTRO_DE_OLEO);
            assertThat(entity.getQuantity()).isEqualTo(3);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(item.getUnitPrice());
            assertThat(entity.getQuote()).isEqualTo(quoteEntity);
        }

        @Test
        void shouldPreserveTimestamps() {
            var item = QuoteStockItem.create(UUID.randomUUID(), FILTRO_DE_OLEO, new BigDecimal(PRICE_100), 1);
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteStockItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getCreatedAt()).isEqualTo(item.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(item.getUpdatedAt());
        }
    }

    private QuoteStockItemEntity validEntity(UUID stockId) {
        return QuoteStockItemEntity.builder()
                .id(UUID.randomUUID())
                .stockId(stockId)
                .productName(FILTRO_DE_OLEO)
                .unitPrice(new BigDecimal(PRICE_100))
                .quantity(2)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}
