package br.com.ofisy.adapters.gateways.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JpaServiceOrderExecutionRepository extends JpaRepository<ServiceOrderExecutionEntity, UUID> {
    Page<ServiceOrderExecutionEntity> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable);

    Page<ServiceOrderExecutionEntity> findByStatus(ServiceOrderExecutionStatus status, Pageable pageable);

    Page<ServiceOrderExecutionEntity> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);

    long countByServiceOrderId(UUID serviceOrderId);

    long countByServiceOrderIdAndStatus(UUID serviceOrderId, ServiceOrderExecutionStatus serviceOrderExecutionStatus);

    List<ServiceOrderExecutionEntity> findByServiceOrderIdAndStatusIn(UUID serviceOrderId, Collection<ServiceOrderExecutionStatus> statuses);
}

