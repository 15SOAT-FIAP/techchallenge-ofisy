package br.com.ofisy.infrastructure.persistence.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JpaServiceOrderExecutionRepository extends JpaRepository<ServiceOrderExecution, UUID> {
    Page<ServiceOrderExecution> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable);

    Page<ServiceOrderExecution> findByStatus(ServiceOrderExecutionStatus status, Pageable pageable);

    Page<ServiceOrderExecution> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);

    long countByServiceOrderId(UUID serviceOrderId);

    long countByServiceOrderIdAndStatus(UUID serviceOrderId, ServiceOrderExecutionStatus serviceOrderExecutionStatus);

    List<ServiceOrderExecution> findByServiceOrderIdAndStatusIn(UUID serviceOrderId, Collection<ServiceOrderExecutionStatus> statuses);
}

