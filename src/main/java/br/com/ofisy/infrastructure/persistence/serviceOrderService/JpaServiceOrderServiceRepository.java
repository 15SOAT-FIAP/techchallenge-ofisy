package br.com.ofisy.infrastructure.persistence.serviceOrderService;

import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaServiceOrderServiceRepository extends JpaRepository<ServiceOrderService, UUID> {
    Page<ServiceOrderService> findByServiceId(UUID serviceId, Pageable pageable);

    Page<ServiceOrderService> findByStatus(ServiceOrderServiceStatus status, Pageable pageable);

    Page<ServiceOrderService> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);
}

