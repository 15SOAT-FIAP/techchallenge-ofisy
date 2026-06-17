package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
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
class QuoteServiceItemMapperTest {

    public static final String PRICE_150 = "150.00";

    @Mock
    private ServiceOrderExecutionRepository executionRepository;

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var executionId = UUID.randomUUID();
            var execution = validExecution();
            var entity = validEntity(executionId);

            when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

            var item = QuoteServiceItemMapper.toDomain(entity, executionRepository);

            assertThat(item.getId()).isEqualTo(entity.getId());
            assertThat(item.getServiceOrderExecution()).isEqualTo(execution);
            assertThat(item.getPrice()).isEqualByComparingTo(entity.getPrice());
        }

        @Test
        void shouldPreserveTimestamps() {
            var executionId = UUID.randomUUID();
            var execution = validExecution();
            var entity = validEntity(executionId);

            when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

            var item = QuoteServiceItemMapper.toDomain(entity, executionRepository);

            assertThat(item.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(item.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldThrowWhenExecutionNotFound() {
            var executionId = UUID.randomUUID();
            var entity = validEntity(executionId);

            when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> QuoteServiceItemMapper.toDomain(entity, executionRepository))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(executionId.toString());
        }
    }

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var execution = validExecution();
            var item = QuoteServiceItem.create(execution, new BigDecimal(PRICE_150));
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteServiceItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getServiceOrderExecutionId()).isEqualTo(execution.getId());
            assertThat(entity.getPrice()).isEqualByComparingTo(item.getPrice());
            assertThat(entity.getQuote()).isEqualTo(quoteEntity);
        }

        @Test
        void shouldPreserveTimestamps() {
            var execution = validExecution();
            var item = QuoteServiceItem.create(execution, new BigDecimal(PRICE_150));
            var quoteEntity = QuoteEntity.builder().id(UUID.randomUUID()).build();

            var entity = QuoteServiceItemMapper.toEntity(item, quoteEntity);

            assertThat(entity.getCreatedAt()).isEqualTo(item.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(item.getUpdatedAt());
        }
    }

    private ServiceOrderExecution validExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    }

    private QuoteServiceItemEntity validEntity(UUID executionId) {
        return QuoteServiceItemEntity.builder()
                .id(UUID.randomUUID())
                .serviceOrderExecutionId(executionId)
                .price(new BigDecimal(PRICE_150))
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}