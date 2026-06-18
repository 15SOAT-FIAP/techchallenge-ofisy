package br.com.ofisy.adapters.gateways.servicecatalog;

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
        ServiceCatalogEntity entity = ServiceCatalogMapper.toEntity(serviceCatalog);
        return ServiceCatalogMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Page<ServiceCatalog> findAll(Pageable pageable) {
        Page<ServiceCatalogEntity> entities = jpa.findAll(pageable);
        return entities.map(ServiceCatalogMapper::toDomain);
    }

    @Override
    public Optional<ServiceCatalog> findByName(String name) {
        Optional<ServiceCatalogEntity> entityOpt = jpa.findByName(name);
        return entityOpt.map(ServiceCatalogMapper::toDomain);
    }

    @Override
    public Optional<ServiceCatalog> findById(UUID id) {
        Optional<ServiceCatalogEntity> entityOpt = jpa.findById(id);
        return entityOpt.map(ServiceCatalogMapper::toDomain);
    }
}
