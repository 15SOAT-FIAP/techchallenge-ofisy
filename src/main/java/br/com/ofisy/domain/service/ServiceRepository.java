package br.com.ofisy.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository {

    Service save(Service service);

    Page<Service> findAll(Pageable pageable);

    Optional<Service> findById(UUID id);

    Page<Service> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Service> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable);

    Page<Service> findByStatus(ServiceStatus status, Pageable pageable);
}

