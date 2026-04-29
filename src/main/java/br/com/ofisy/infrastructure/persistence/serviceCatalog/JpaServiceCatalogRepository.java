package br.com.ofisy.infrastructure.persistence.serviceCatalog;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID> {
    Optional<ServiceCatalog> findByName(String name);
}
