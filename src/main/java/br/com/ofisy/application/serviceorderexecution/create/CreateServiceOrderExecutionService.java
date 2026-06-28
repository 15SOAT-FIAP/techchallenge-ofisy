package br.com.ofisy.application.serviceorderexecution.create;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateServiceOrderExecutionService implements CreateServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public CreateServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrderExecution execute(CreateServiceOrderExecutionCommand cmd) {
        ServiceOrderExecution serviceOrderExecution = ServiceOrderExecution.create(
                cmd.serviceCatalogId(),
                cmd.serviceOrderId()
        );

        return repository.save(serviceOrderExecution);
    }
}

