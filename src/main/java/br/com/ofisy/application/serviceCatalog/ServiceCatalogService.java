package br.com.ofisy.application.serviceCatalog;

import br.com.ofisy.application.serviceCatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.serviceCatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.serviceCatalog.ServiceCatalog;
import br.com.ofisy.domain.serviceCatalog.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceCatalogRepository repository;

    public ServiceCatalog create(ServiceCatalogRequestDTO dto) {
        ServiceCatalog serviceCatalog = ServiceCatalog.create(
                dto.name(),
                dto.description(),
                dto.price()
        );

        return repository.save(serviceCatalog);
    }

    public ServiceCatalog findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceCatalogNotFoundException(id.toString()));
    }

    public Page<ServiceCatalog> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ServiceCatalog findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ServiceCatalogNotFoundException(name));
    }
}
