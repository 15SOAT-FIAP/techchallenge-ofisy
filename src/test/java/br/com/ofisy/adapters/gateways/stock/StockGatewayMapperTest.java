package br.com.ofisy.adapters.gateways.stock;

import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StockGatewayMapperTest {

    private static final String PRODUCT_NAME = "Filtro de Óleo";
    private static final String DESCRIPTION = "Filtro de óleo para motor";
    private static final String CATEGORY = "Lubrificação";
    private static final Integer QUANTITY = 15;
    private static final Integer MIN_THRESHOLD = 5;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("29.90");

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var stock = Stock.create(
                    PRODUCT_NAME,
                    DESCRIPTION,
                    QUANTITY,
                    UNIT_PRICE,
                    CATEGORY,
                    MIN_THRESHOLD
            );

            var entity = StockMapper.toEntity(stock);

            assertThat(entity).isNotNull();
            assertThat(entity.getProductName()).isEqualTo(PRODUCT_NAME);
            assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(entity.getQuantity()).isEqualTo(QUANTITY);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
            assertThat(entity.getCategory()).isEqualTo(CATEGORY);
            assertThat(entity.getMinThreshold()).isEqualTo(MIN_THRESHOLD);
        }

        @Test
        void shouldPreserveNullIdForNewStock() {
            var stock = Stock.create(
                    PRODUCT_NAME,
                    DESCRIPTION,
                    QUANTITY,
                    UNIT_PRICE,
                    CATEGORY,
                    MIN_THRESHOLD
            );

            var entity = StockMapper.toEntity(stock);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedStock() {
            var id = UUID.randomUUID();

            var stock = Stock.reconstruct(
                    id,
                    PRODUCT_NAME,
                    DESCRIPTION,
                    QUANTITY,
                    UNIT_PRICE,
                    CATEGORY,
                    MIN_THRESHOLD,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            var entity = StockMapper.toEntity(stock);

            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        void shouldPreserveInventoryValuesWhenMappingToEntity() {
            var stock = Stock.create(
                    PRODUCT_NAME,
                    DESCRIPTION,
                    QUANTITY,
                    UNIT_PRICE,
                    CATEGORY,
                    MIN_THRESHOLD
            );

            var entity = StockMapper.toEntity(stock);

            assertThat(entity.getQuantity()).isEqualTo(QUANTITY);
            assertThat(entity.getMinThreshold()).isEqualTo(MIN_THRESHOLD);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2026, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2026, 1, 15, 12, 0);

            var stock = Stock.reconstruct(
                    UUID.randomUUID(),
                    PRODUCT_NAME,
                    DESCRIPTION,
                    QUANTITY,
                    UNIT_PRICE,
                    CATEGORY,
                    MIN_THRESHOLD,
                    createdAt,
                    updatedAt
            );

            var entity = StockMapper.toEntity(stock);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var stock = StockMapper.toDomain(entity);

            assertThat(stock).isNotNull();
            assertThat(stock.getId()).isEqualTo(entity.getId());
            assertThat(stock.getProductName()).isEqualTo(entity.getProductName());
            assertThat(stock.getDescription()).isEqualTo(entity.getDescription());
            assertThat(stock.getQuantity()).isEqualTo(entity.getQuantity());
            assertThat(stock.getUnitPrice()).isEqualByComparingTo(entity.getUnitPrice());
            assertThat(stock.getCategory()).isEqualTo(entity.getCategory());
            assertThat(stock.getMinThreshold()).isEqualTo(entity.getMinThreshold());
        }

        @Test
        void shouldPreserveInventoryValuesWhenMappingToDomain() {
            var entity = validEntity();

            var stock = StockMapper.toDomain(entity);

            assertThat(stock.getQuantity()).isEqualTo(QUANTITY);
            assertThat(stock.getMinThreshold()).isEqualTo(MIN_THRESHOLD);
            assertThat(stock.getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var stock = StockMapper.toDomain(entity);

            assertThat(stock.getId()).isEqualTo(entity.getId());
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var stock = StockMapper.toDomain(entity);

            assertThat(stock.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(stock.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    private StockEntity validEntity() {
        return StockEntity.builder()
                .id(UUID.randomUUID())
                .productName(PRODUCT_NAME)
                .description(DESCRIPTION)
                .quantity(QUANTITY)
                .unitPrice(UNIT_PRICE)
                .category(CATEGORY)
                .minThreshold(MIN_THRESHOLD)
                .createdAt(LocalDateTime.of(2026, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 15, 12, 0))
                .build();
    }
}
