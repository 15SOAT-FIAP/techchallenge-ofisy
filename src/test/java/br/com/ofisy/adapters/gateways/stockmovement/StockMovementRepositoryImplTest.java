package br.com.ofisy.adapters.gateways.stockmovement;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockMovementRepositoryImplTest {

    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final MovementType MOVEMENT_TYPE = MovementType.IN;
    private static final Integer QUANTITY = 10;
    private static final Integer PREVIOUS_QUANTITY = 40;
    private static final Integer NEW_QUANTITY = 50;

    @Mock
    private JpaStockMovementRepository jpa;

    @InjectMocks
    private StockMovementRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntityBeforeSaving() {
            var domain = validDomain();
            var savedEntity = validEntity();

            when(jpa.save(any(StockMovementEntity.class))).thenReturn(savedEntity);

            repository.save(domain);

            var captor = ArgumentCaptor.forClass(StockMovementEntity.class);

            verify(jpa).save(captor.capture());

            assertThat(captor.getValue().getStockId()).isEqualTo(STOCK_ID);
            assertThat(captor.getValue().getMovementType()).isEqualTo(MOVEMENT_TYPE);
            assertThat(captor.getValue().getQuantity()).isEqualTo(QUANTITY);
            assertThat(captor.getValue().getPreviousQuantity()).isEqualTo(PREVIOUS_QUANTITY);
            assertThat(captor.getValue().getNewQuantity()).isEqualTo(NEW_QUANTITY);
        }

        @Test
        void shouldReturnDomainStockMovementWithIdAfterSave() {
            var domain = validDomain();
            var savedEntity = validEntity();

            when(jpa.save(any(StockMovementEntity.class))).thenReturn(savedEntity);

            var result = repository.save(domain);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedEntity.getId());
            assertThat(result.getStockId()).isEqualTo(savedEntity.getStockId());
            assertThat(result.getMovementType()).isEqualTo(savedEntity.getMovementType());
            assertThat(result.getQuantity()).isEqualTo(savedEntity.getQuantity());
            assertThat(result.getPreviousQuantity()).isEqualTo(savedEntity.getPreviousQuantity());
            assertThat(result.getNewQuantity()).isEqualTo(savedEntity.getNewQuantity());
        }

        @Test
        void shouldReturnStockMovementWithTimestampsFromPersistedEntity() {
            var domain = validDomain();
            var savedEntity = validEntity();

            when(jpa.save(any(StockMovementEntity.class))).thenReturn(savedEntity);

            var result = repository.save(domain);

            assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnPageOfDomainStockMovementsMappedFromEntities() {
            var pageable = PageRequest.of(0, 10);
            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            var movement = result.getContent().get(0);

            assertThat(movement.getId()).isEqualTo(entity.getId());
            assertThat(movement.getStockId()).isEqualTo(entity.getStockId());

            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoStockMovements() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<StockMovementEntity>(List.of(), pageable, 0);

            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindByStockId {

        @Test
        void shouldReturnPageOfMovementsForGivenStockId() {
            var pageable = PageRequest.of(0, 10);
            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);

            when(jpa.findByStockId(STOCK_ID, pageable)).thenReturn(page);

            var result = repository.findByStockId(STOCK_ID, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStockId()).isEqualTo(STOCK_ID);

            verify(jpa).findByStockId(STOCK_ID, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoMovementsForStockId() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<StockMovementEntity>(List.of(), pageable, 0);

            when(jpa.findByStockId(STOCK_ID, pageable)).thenReturn(emptyPage);

            var result = repository.findByStockId(STOCK_ID, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    private StockMovement validDomain() {
        return StockMovement.create(STOCK_ID, MOVEMENT_TYPE, QUANTITY, PREVIOUS_QUANTITY, NEW_QUANTITY);
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
