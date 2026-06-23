package br.com.ofisy.adapters.presenters.servicecatalog;

import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceCatalogPresenter {

    public static ServiceCatalogResponseDTO present(ServiceCatalog serviceCatalog) {
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
