package br.com.ofisy.application.serviceorderexecution.identifybyid;

import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IdentifyByIdServiceOrderExecutionService implements IdentifyByIdServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public IdentifyByIdServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrderExecution execute(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
    }
}

