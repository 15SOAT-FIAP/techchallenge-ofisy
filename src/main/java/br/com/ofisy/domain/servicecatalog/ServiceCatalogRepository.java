package br.com.ofisy.domain.servicecatalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogRepository {

    ServiceCatalog save(ServiceCatalog serviceCatalog);

    Page<ServiceCatalog> findAll(Pageable pageable);

    Optional<ServiceCatalog> findById(UUID id);

    Optional<ServiceCatalog> findByName(String name);
}
