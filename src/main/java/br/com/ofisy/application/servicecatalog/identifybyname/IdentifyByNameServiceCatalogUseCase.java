package br.com.ofisy.application.servicecatalog.identifybyname;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;

public interface IdentifyByNameServiceCatalogUseCase {
    ServiceCatalog execute(String name);

}
