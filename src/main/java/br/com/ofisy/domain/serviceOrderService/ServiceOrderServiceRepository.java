package br.com.ofisy.domain.serviceOrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderServiceRepository {

    ServiceOrderService save(ServiceOrderService serviceOrderService);

    Page<ServiceOrderService> findAll(Pageable pageable);

    Optional<ServiceOrderService> findById(UUID id);

    Page<ServiceOrderService> findByServiceId(UUID serviceId, Pageable pageable);

    Page<ServiceOrderService> findByServiceOrderId(UUID serviceOrderId, Pageable pageable);

    Page<ServiceOrderService> findByStatus(ServiceOrderServiceStatus status, Pageable pageable);
}

