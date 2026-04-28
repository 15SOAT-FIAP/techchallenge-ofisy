package br.com.ofisy.infrastructure.persistence.serviceOrderExecution;

import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaServiceOrderExecutionRepository extends JpaRepository<ServiceOrderExecution, UUID> {
    Page<ServiceOrderExecution> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable);

    Page<ServiceOrderExecution> findByStatus(ServiceOrderExecutionStatus status, Pageable pageable);

    Page<ServiceOrderExecution> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);
}

