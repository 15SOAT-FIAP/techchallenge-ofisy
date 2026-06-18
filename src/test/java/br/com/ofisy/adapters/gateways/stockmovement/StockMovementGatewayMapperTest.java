package br.com.ofisy.adapters.gateways.stockmovement;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StockMovementGatewayMapperTest {

    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final MovementType MOVEMENT_TYPE = MovementType.IN;
    private static final Integer QUANTITY = 10;
    private static final Integer PREVIOUS_QUANTITY = 40;
    private static final Integer NEW_QUANTITY = 50;

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var domain = StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);

            var entity = StockMovementMapper.toEntity(domain);

            assertThat(entity).isNotNull();
            assertThat(entity.getStockId()).isEqualTo(STOCK_ID);
            assertThat(entity.getMovementType()).isEqualTo(MOVEMENT_TYPE);
            assertThat(entity.getQuantity()).isEqualTo(QUANTITY);
            assertThat(entity.getPreviousQuantity()).isEqualTo(PREVIOUS_QUANTITY);
            assertThat(entity.getNewQuantity()).isEqualTo(NEW_QUANTITY);
        }

        @Test
        void shouldPreserveNullIdForNewStockMovement() {
            var domain = StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);

            var entity = StockMovementMapper.toEntity(domain);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedStockMovement() {
            var id = UUID.randomUUID();
            var domain = StockMovement.reconstruct(
                    id, STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            var entity = StockMovementMapper.toEntity(domain);

            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2026, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2026, 1, 15, 12, 0);
            var domain = StockMovement.reconstruct(
                    UUID.randomUUID(), STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY,
                    createdAt, updatedAt
            );

            var entity = StockMovementMapper.toEntity(domain);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var domain = StockMovementMapper.toDomain(entity);

            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isEqualTo(entity.getId());
            assertThat(domain.getStockId()).isEqualTo(entity.getStockId());
            assertThat(domain.getMovementType()).isEqualTo(entity.getMovementType());
            assertThat(domain.getQuantity()).isEqualTo(entity.getQuantity());
            assertThat(domain.getPreviousQuantity()).isEqualTo(entity.getPreviousQuantity());
            assertThat(domain.getNewQuantity()).isEqualTo(entity.getNewQuantity());
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var domain = StockMovementMapper.toDomain(entity);

            assertThat(domain.getId()).isEqualTo(entity.getId());
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var domain = StockMovementMapper.toDomain(entity);

            assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(domain.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    private StockMovementEntity validEntity() {
        return StockMovementEntity.builder()
                .id(UUID.randomUUID())
                .stockId(STOCK_ID)
                .movementType(MOVEMENT_TYPE)
                .quantity(QUANTITY)
                .previousQuantity(PREVIOUS_QUANTITY)
                .newQuantity(NEW_QUANTITY)
                .createdAt(LocalDateTime.of(2026, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 15, 12, 0))
                .build();
    }
}
