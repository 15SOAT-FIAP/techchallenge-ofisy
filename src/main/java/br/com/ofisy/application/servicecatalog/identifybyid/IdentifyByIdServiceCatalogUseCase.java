package br.com.ofisy.application.servicecatalog.identifybyid;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;

import java.util.UUID;

public interface IdentifyByIdServiceCatalogUseCase {
    ServiceCatalog execute(UUID id);
}
