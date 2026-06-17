package br.com.ofisy.adapters.presenters.stockmovement;

import br.com.ofisy.adapters.controllers.stockmovement.dto.StockMovementResponseDTO;
import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementPresenterTest {

    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final MovementType MOVEMENT_TYPE = MovementType.IN;
    private static final Integer QUANTITY = 10;
    private static final Integer PREVIOUS_QUANTITY = 40;
    private static final Integer NEW_QUANTITY = 50;

    @Nested
    class Present {

        @Test
        void shouldMapAllFieldsCorrectly() {
            var domain = StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);

            StockMovementResponseDTO dto = StockMovementPresenter.present(domain);

            assertThat(dto).isNotNull();
            assertThat(dto.stockId()).isEqualTo(STOCK_ID);
            assertThat(dto.movementType()).isEqualTo(MOVEMENT_TYPE);
            assertThat(dto.quantity()).isEqualTo(QUANTITY);
            assertThat(dto.previousQuantity()).isEqualTo(PREVIOUS_QUANTITY);
            assertThat(dto.newQuantity()).isEqualTo(NEW_QUANTITY);
        }

        @Test
        void shouldSetTimestampsFromCreatedMovement() {
            var domain = StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);

            var dto = StockMovementPresenter.present(domain);

            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldPreserveIdAndTimestampsFromReconstructedMovement() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2026, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2026, 1, 15, 12, 0);
            var domain = StockMovement.reconstruct(id, STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY,
                    createdAt, updatedAt);

            var dto = StockMovementPresenter.present(domain);

            assertThat(dto.id()).isEqualTo(id);
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldHaveNullIdForNewMovement() {
            var domain = StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);

            var dto = StockMovementPresenter.present(domain);

            assertThat(dto.id()).isNull();
        }
    }
}
