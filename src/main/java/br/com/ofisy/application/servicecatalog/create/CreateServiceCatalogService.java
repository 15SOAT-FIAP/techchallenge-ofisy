package br.com.ofisy.application.servicecatalog.create;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateServiceCatalogService implements CreateServiceCatalogUseCase {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public CreateServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    @Override
    public ServiceCatalog execute(CreateServiceCatalogCommand cmd) {
        ServiceCatalog serviceCatalog = ServiceCatalog.create(
                cmd.name(),
                cmd.description(),
                cmd.price()
        );

        return serviceCatalogRepository.save(serviceCatalog);
    }
}
