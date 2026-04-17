package br.com.ofisy.domain.catalogService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CatalogServiceRepository {

    CatalogService save(CatalogService catalogService);

    Page<CatalogService> findAll(Pageable pageable);

    Optional<CatalogService> findById(UUID id);

    Optional<CatalogService> findByName(String name);
}
