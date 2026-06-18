package br.com.ofisy.application.serviceorderexecution.listbyserviceorderid;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListByServiceOrderIdServiceOrderExecutionService implements ListByServiceOrderIdServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public ListByServiceOrderIdServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ServiceOrderExecution> execute(UUID serviceOrderId, Pageable pageable) {
        if (serviceOrderId == null) {
            throw new IllegalArgumentException("Service Order ID não pode ser nulo");
        }
        return repository.findByServiceOrderId(serviceOrderId, pageable);
    }
}

