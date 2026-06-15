package br.com.ofisy.adapters.gateways.stock;

import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockRepositoryImplTest {

    private static final String PRODUCT_NAME = "Filtro de Óleo";
    private static final String DESCRIPTION = "Filtro de óleo para motor";
    private static final String CATEGORY = "Lubrificação";
    private static final Integer QUANTITY = 15;
    private static final Integer MIN_THRESHOLD = 5;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("29.90");

    @Mock
    private JpaStockRepository jpa;

    @InjectMocks
    private StockRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntityBeforeSaving() {
            var stock = validStock();
            var savedEntity = validEntity();

            when(jpa.save(any(StockEntity.class))).thenReturn(savedEntity);

            repository.save(stock);

            var captor = ArgumentCaptor.forClass(StockEntity.class);

            verify(jpa).save(captor.capture());

            assertThat(captor.getValue().getProductName()).isEqualTo(PRODUCT_NAME);
            assertThat(captor.getValue().getDescription()).isEqualTo(DESCRIPTION);
            assertThat(captor.getValue().getQuantity()).isEqualTo(QUANTITY);
            assertThat(captor.getValue().getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
            assertThat(captor.getValue().getCategory()).isEqualTo(CATEGORY);
            assertThat(captor.getValue().getMinThreshold()).isEqualTo(MIN_THRESHOLD);
        }

        @Test
        void shouldReturnDomainStockWithIdAfterSave() {
            var stock = validStock();
            var savedEntity = validEntity();

            when(jpa.save(any(StockEntity.class))).thenReturn(savedEntity);

            var result = repository.save(stock);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedEntity.getId());
            assertThat(result.getProductName()).isEqualTo(savedEntity.getProductName());
            assertThat(result.getDescription()).isEqualTo(savedEntity.getDescription());
            assertThat(result.getQuantity()).isEqualTo(savedEntity.getQuantity());
            assertThat(result.getUnitPrice()).isEqualByComparingTo(savedEntity.getUnitPrice());
            assertThat(result.getCategory()).isEqualTo(savedEntity.getCategory());
            assertThat(result.getMinThreshold()).isEqualTo(savedEntity.getMinThreshold());
        }

        @Test
        void shouldReturnStockWithTimestampsFromPersistedEntity() {
            var stock = validStock();
            var savedEntity = validEntity();

            when(jpa.save(any(StockEntity.class))).thenReturn(savedEntity);

            var result = repository.save(stock);

            assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnPageOfDomainStocksMappedFromEntities() {
            var pageable = PageRequest.of(0, 10);

            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);

            var stock = result.getContent().get(0);

            assertThat(stock.getId()).isEqualTo(entity.getId());
            assertThat(stock.getProductName()).isEqualTo(entity.getProductName());

            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoStockItems() {
            var pageable = PageRequest.of(0, 10);

            var emptyPage = new PageImpl<StockEntity>(List.of(), pageable, 0);

            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDomainStockWhenEntityFound() {
            var id = UUID.randomUUID();
            var entity = validEntity();

            when(jpa.findById(id)).thenReturn(Optional.of(entity));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(entity.getId());
            assertThat(result.get().getProductName()).isEqualTo(entity.getProductName());
            assertThat(result.get().getDescription()).isEqualTo(entity.getDescription());
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();

            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByProductName {

        @Test
        void shouldReturnDomainStockWhenFound() {
            var entity = validEntity();

            when(jpa.findByProductName(PRODUCT_NAME))
                    .thenReturn(Optional.of(entity));

            var result = repository.findByProductName(PRODUCT_NAME);

            assertThat(result).isPresent();
            assertThat(result.get().getProductName()).isEqualTo(PRODUCT_NAME);
            assertThat(result.get().getCategory()).isEqualTo(CATEGORY);
        }

        @Test
        void shouldPreserveInventoryDataWhenEntityFound() {
            var entity = validEntity();

            when(jpa.findByProductName(PRODUCT_NAME))
                    .thenReturn(Optional.of(entity));

            var result = repository.findByProductName(PRODUCT_NAME);

            assertThat(result).isPresent();
            assertThat(result.get().getQuantity()).isEqualTo(QUANTITY);
            assertThat(result.get().getMinThreshold()).isEqualTo(MIN_THRESHOLD);
            assertThat(result.get().getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            when(jpa.findByProductName(PRODUCT_NAME))
                    .thenReturn(Optional.empty());

            var result = repository.findByProductName(PRODUCT_NAME);

            assertThat(result).isEmpty();

            verify(jpa).findByProductName(PRODUCT_NAME);
        }
    }

    private Stock validStock() {
        return Stock.create(
                PRODUCT_NAME,
                DESCRIPTION,
                QUANTITY,
                UNIT_PRICE,
                CATEGORY,
                MIN_THRESHOLD
        );
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
