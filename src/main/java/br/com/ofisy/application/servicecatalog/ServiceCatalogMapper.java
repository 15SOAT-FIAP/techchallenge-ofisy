package br.com.ofisy.application.servicecatalog;

import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceCatalogMapper {

    public static ServiceCatalog toDomain(ServiceCatalogRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("ServiceCatalogRequestDTO não pode ser nulo");
        }

        return ServiceCatalog.create(
                request.name(),
                request.description(),
                request.price()
        );
    }

    public static ServiceCatalogResponseDTO toDTO(ServiceCatalog serviceCatalog) {
        if (serviceCatalog == null) {
            throw new IllegalArgumentException("ServiceCatalog não pode ser nulo");
        }

        return new ServiceCatalogResponseDTO(
                serviceCatalog.getId(),
                serviceCatalog.getName(),
                serviceCatalog.getDescription(),
                serviceCatalog.getPrice(),
                serviceCatalog.getCreatedAt(),
                serviceCatalog.getUpdatedAt()
        );
    }
}
