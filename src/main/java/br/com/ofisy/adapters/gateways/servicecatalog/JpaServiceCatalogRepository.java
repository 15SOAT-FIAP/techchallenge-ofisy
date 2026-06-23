package br.com.ofisy.adapters.gateways.servicecatalog;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaServiceCatalogRepository extends JpaRepository<ServiceCatalogEntity, UUID> {
    Optional<ServiceCatalogEntity> findByName(String name);
}
