package br.com.ofisy.application.servicecatalog.list;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListServiceCatalogService implements ListServiceCatalogUseCase {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ListServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    @Override
    public Page<ServiceCatalog> execute(Pageable pageable) {
        return serviceCatalogRepository.findAll(pageable);
    }
}
