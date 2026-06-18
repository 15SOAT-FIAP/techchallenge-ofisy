package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteStockItemMapperTest {

    public static final String PRICE_100 = "100.00";
    @Mock
    private StockRepository stockRepository;

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var stockId = UUID.randomUUID();
            var stock = validStock(stockId);
            var entity = validEntity(stockId);

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

            var item = QuoteStockItemMapper.toDomain(entity, stockRepository);

            assertThat(item.getId()).isEqualTo(entity.getId());
            assertThat(item.getStock()).isEqualTo(stock);
            assertThat(item.getUnitPrice()).isEqualByComparingTo(entity.getUnitPrice());
            assertThat(item.getQuantity()).isEqualTo(entity.getQuantity());
        }

        @Test
        void shouldPreserveTimestamps() {
            var stockId = UUID.randomUUID();
            var stock = validStock(stockId);
            var entity = validEntity(stockId);

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));

            var item = QuoteStockItemMapper.toDomain(entity, stockRepository);

            assertThat(item.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(item.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldThrowWhenStockNotFound() {
            var stockId = UUID.randomUUID();
            var entity = validEntity(stockId);

            when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> QuoteStockItemMapper.toDomain(entity, stockRepository))
                    .isInstanceOf(StockNotFoundException.class)
                    .hasMessageContaining(stockId.toString());
        }
    }

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var stockId = UUID.randomUUID();
            var stock = validStock(stockId);
            var item = QuoteStockItem.create(stock, 3);
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteStockItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getStockId()).isEqualTo(stockId);
            assertThat(entity.getQuantity()).isEqualTo(3);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(item.getUnitPrice());
            assertThat(entity.getQuote()).isEqualTo(quoteEntity);
        }

        @Test
        void shouldPreserveTimestamps() {
            var stock = validStock(UUID.randomUUID());
            var item = QuoteStockItem.create(stock, 1);
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteStockItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getCreatedAt()).isEqualTo(item.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(item.getUpdatedAt());
        }
    }

    private Stock validStock(UUID id) {
        return Stock.reconstruct(id, "Filtro de óleo", "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private QuoteStockItemEntity validEntity(UUID stockId) {
        return QuoteStockItemEntity.builder()
                .id(UUID.randomUUID())
                .stockId(stockId)
                .unitPrice(new BigDecimal(PRICE_100))
                .quantity(2)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}
