package br.com.ofisy.infrastructure.persistence.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
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
}
