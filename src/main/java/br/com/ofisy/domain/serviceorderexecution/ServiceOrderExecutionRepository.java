package br.com.ofisy.domain.serviceorderexecution;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderExecutionRepository {

    ServiceOrderExecution save(ServiceOrderExecution serviceOrderExecution);

    Page<ServiceOrderExecution> findAll(Pageable pageable);

    Optional<ServiceOrderExecution> findById(UUID id);

    Page<ServiceOrderExecution> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable);

    Page<ServiceOrderExecution> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);

    Page<ServiceOrderExecution> findByStatus(ServiceOrderExecutionStatus status, Pageable pageable);
}

