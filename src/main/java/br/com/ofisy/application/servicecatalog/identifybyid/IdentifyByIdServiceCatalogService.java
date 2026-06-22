package br.com.ofisy.application.servicecatalog.identifybyid;

import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IdentifyByIdServiceCatalogService implements IdentifyByIdServiceCatalogUseCase {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public IdentifyByIdServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    @Override
    public ServiceCatalog execute(UUID id) {
        return serviceCatalogRepository.findById(id)
                .orElseThrow(() -> ServiceCatalogNotFoundException.ofId(id.toString()));
    }

}
