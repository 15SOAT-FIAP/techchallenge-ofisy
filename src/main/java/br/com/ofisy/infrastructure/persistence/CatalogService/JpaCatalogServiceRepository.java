package br.com.ofisy.infrastructure.persistence.CatalogService;

import br.com.ofisy.domain.catalogService.CatalogService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCatalogServiceRepository extends JpaRepository<CatalogService, UUID> {
    Optional<CatalogService> findByName(String name);
}
