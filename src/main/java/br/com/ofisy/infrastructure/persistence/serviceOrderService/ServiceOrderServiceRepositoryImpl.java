package br.com.ofisy.infrastructure.persistence.serviceOrderService;

import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceRepository;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceOrderServiceRepositoryImpl implements ServiceOrderServiceRepository {

    private final JpaServiceOrderServiceRepository jpa;

    @Override
    public ServiceOrderService save(ServiceOrderService serviceOrderService) {
        return jpa.save(serviceOrderService);
    }

    @Override
    public Page<ServiceOrderService> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<ServiceOrderService> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<ServiceOrderService> findByServiceId(UUID serviceId, Pageable pageable) {
        return jpa.findByServiceId(serviceId, pageable);
    }

    @Override
    public Page<ServiceOrderService> findByServiceOrderId(UUID serviceOrderId, Pageable pageable) {
        return jpa.findByServiceOrderId(serviceOrderId, pageable);
    }

    @Override
    public Page<ServiceOrderService> findByStatus(ServiceOrderServiceStatus status, Pageable pageable) {
        return jpa.findByStatus(status, pageable);
    }
}

