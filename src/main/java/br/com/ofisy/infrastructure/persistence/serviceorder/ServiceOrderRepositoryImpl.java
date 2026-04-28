package br.com.ofisy.infrastructure.persistence.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceOrderRepositoryImpl implements ServiceOrderRepository {

    private final JpaServiceOrderRepository jpa;

    @Override
    public ServiceOrder save(ServiceOrder serviceOrder) {
        return jpa.save(serviceOrder);
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<ServiceOrder> findByStatus(ServiceOrderStatus serviceOrderStatus, Pageable pageable) {
        return jpa.findByStatus(serviceOrderStatus, pageable);
    }
}
