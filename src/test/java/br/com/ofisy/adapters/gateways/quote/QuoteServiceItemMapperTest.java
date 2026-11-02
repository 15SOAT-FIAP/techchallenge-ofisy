package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteServiceItem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceItemMapperTest {

    public static final String PRICE_150 = "150.00";

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var serviceCatalogId = UUID.randomUUID();
            var entity = validEntity(serviceCatalogId);

            var item = QuoteServiceItemMapper.toDomain(entity);

            assertThat(item.getId()).isEqualTo(entity.getId());
            assertThat(item.getServiceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(item.getPrice()).isEqualByComparingTo(entity.getPrice());
        }

        @Test
        void shouldPreserveTimestamps() {
            var entity = validEntity(UUID.randomUUID());

            var item = QuoteServiceItemMapper.toDomain(entity);

            assertThat(item.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(item.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var serviceCatalogId = UUID.randomUUID();
            var item = QuoteServiceItem.create(serviceCatalogId, new BigDecimal(PRICE_150));
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteServiceItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getServiceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(entity.getPrice()).isEqualByComparingTo(item.getPrice());
            assertThat(entity.getQuote()).isEqualTo(quoteEntity);
        }

        @Test
        void shouldPreserveTimestamps() {
            var item = QuoteServiceItem.create(UUID.randomUUID(), new BigDecimal(PRICE_150));
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteServiceItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getCreatedAt()).isEqualTo(item.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(item.getUpdatedAt());
        }
    }

    private QuoteServiceItemEntity validEntity(UUID serviceCatalogId) {
        return QuoteServiceItemEntity.builder()
                .id(UUID.randomUUID())
                .serviceCatalogId(serviceCatalogId)
                .price(new BigDecimal(PRICE_150))
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}