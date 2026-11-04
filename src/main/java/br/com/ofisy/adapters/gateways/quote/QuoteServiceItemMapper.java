package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.QuoteServiceItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteServiceItemMapper {

    public static QuoteServiceItem toDomain(QuoteServiceItemEntity entity) {
        return QuoteServiceItem.reconstruct(
                entity.getId(),
                entity.getServiceCatalogId(),
                entity.getPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static QuoteServiceItemEntity toEntity(QuoteServiceItem item, QuoteEntity quoteEntity) {
        return QuoteServiceItemEntity.builder()
                .id(item.getId())
                .quote(quoteEntity)
                .serviceCatalogId(item.getServiceCatalogId())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}