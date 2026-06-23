package br.com.ofisy.application.servicecatalog.identifybyname;

import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentifyByNameServiceCatalogService implements IdentifyByNameServiceCatalogUseCase {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public IdentifyByNameServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    @Override
    public ServiceCatalog execute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Nome não pode ser nulo");
        }
        return serviceCatalogRepository.findByName(name)
                .orElseThrow(() -> ServiceCatalogNotFoundException.ofName(name));
    }

}