package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteServiceItemMapper {

    public static QuoteServiceItem toDomain(QuoteServiceItemEntity entity,
                                            ServiceOrderExecutionRepository executionRepository) {
        ServiceOrderExecution execution = executionRepository.findById(entity.getServiceOrderExecutionId())
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(entity.getServiceOrderExecutionId()));

        return QuoteServiceItem.reconstruct(
                entity.getId(),
                execution,
                entity.getPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static QuoteServiceItemEntity toEntity(QuoteServiceItem item, QuoteEntity quoteEntity) {
        return QuoteServiceItemEntity.builder()
                .id(item.getId())
                .quote(quoteEntity)
                .serviceOrderExecutionId(item.getServiceOrderExecution().getId())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}