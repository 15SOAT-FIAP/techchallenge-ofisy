package br.com.ofisy.domain.serviceorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder serviceOrder);
    Optional<ServiceOrder> findById(UUID id);
    Page<ServiceOrder> findByStatus(ServiceOrderStatus serviceOrderStatus, Pageable pageable);
}
