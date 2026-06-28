package br.com.ofisy.adapters.gateways.servicecatalog;


import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceCatalogMapper {

    public static ServiceCatalog toDomain(ServiceCatalogEntity entity) {

        return ServiceCatalog.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ServiceCatalogEntity toEntity(ServiceCatalog serviceCatalog) {
        return ServiceCatalogEntity.builder()
                .id(serviceCatalog.getId())
                .name(serviceCatalog.getName())
                .description(serviceCatalog.getDescription())
                .price(serviceCatalog.getPrice())
                .createdAt(serviceCatalog.getCreatedAt())
                .updatedAt(serviceCatalog.getUpdatedAt())
                .build();
    }

}
