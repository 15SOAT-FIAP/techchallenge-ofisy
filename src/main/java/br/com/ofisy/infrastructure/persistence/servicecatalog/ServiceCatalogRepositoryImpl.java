package br.com.ofisy.infrastructure.persistence.servicecatalog;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceCatalogRepositoryImpl implements ServiceCatalogRepository {

    private final JpaServiceCatalogRepository jpa;

    @Override
    public ServiceCatalog save(ServiceCatalog serviceCatalog) {
        return jpa.save(serviceCatalog);
    }

    @Override
    public Page<ServiceCatalog> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<ServiceCatalog> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<ServiceCatalog> findByName(String name) {
        return jpa.findByName(name);
    }
}
