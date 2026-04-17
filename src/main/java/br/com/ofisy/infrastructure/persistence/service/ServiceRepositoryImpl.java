package br.com.ofisy.infrastructure.persistence.service;

import br.com.ofisy.domain.service.Service;
import br.com.ofisy.domain.service.ServiceRepository;
import br.com.ofisy.domain.service.ServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceRepositoryImpl implements ServiceRepository {

    private final JpaServiceRepository jpa;

    @Override
    public Service save(Service service) {
        return jpa.save(service);
    }

    @Override
    public Page<Service> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<Service> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Service> findByCustomerId(UUID customerId, Pageable pageable) {
        return jpa.findByCustomerId(customerId, pageable);
    }

    @Override
    public Page<Service> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable) {
        return jpa.findByCatalogServiceId(catalogServiceId, pageable);
    }

    @Override
    public Page<Service> findByStatus(ServiceStatus status, Pageable pageable) {
        return jpa.findByStatus(status, pageable);
    }
}

