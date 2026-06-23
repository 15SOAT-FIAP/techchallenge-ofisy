package br.com.ofisy.application.servicecatalog.create;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;

import java.math.BigDecimal;

public interface CreateServiceCatalogUseCase {

    ServiceCatalog execute(CreateServiceCatalogCommand cmd);

    record CreateServiceCatalogCommand(
            String name,
            String description,
            BigDecimal price
    ) {}
}
