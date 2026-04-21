package br.com.ofisy.infrastructure.persistence.service;

import br.com.ofisy.domain.service.Service;
import br.com.ofisy.domain.service.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaServiceRepository extends JpaRepository<Service, UUID> {
    Page<Service> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable);

    Page<Service> findByStatus(ServiceStatus status, Pageable pageable);
}

