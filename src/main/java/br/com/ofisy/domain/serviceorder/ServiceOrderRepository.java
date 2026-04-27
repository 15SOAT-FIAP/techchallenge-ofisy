package br.com.ofisy.domain.serviceorder;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder serviceOrder);
    Optional<ServiceOrder> findById(UUID id);
}
