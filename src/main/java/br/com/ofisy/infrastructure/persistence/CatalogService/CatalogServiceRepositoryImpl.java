package br.com.ofisy.infrastructure.persistence.CatalogService;

import br.com.ofisy.domain.catalogService.CatalogService;
import br.com.ofisy.domain.catalogService.CatalogServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CatalogServiceRepositoryImpl implements CatalogServiceRepository {

    private final JpaCatalogServiceRepository jpa;

    @Override
    public CatalogService save(CatalogService catalogService) {
        return jpa.save(catalogService);
    }

    @Override
    public Page<CatalogService> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<CatalogService> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<CatalogService> findByName(String name) {
        return jpa.findByName(name);
    }
}
