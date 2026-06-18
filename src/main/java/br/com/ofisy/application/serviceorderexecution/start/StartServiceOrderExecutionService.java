package br.com.ofisy.application.serviceorderexecution.start;

import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StartServiceOrderExecutionService implements StartServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;
    private final br.com.ofisy.application.serviceorder.startexecution.StartServiceOrderExecutionUseCase finishServiceOrderExecutionUseCase;

    public StartServiceOrderExecutionService(ServiceOrderExecutionRepository repository,
                                            br.com.ofisy.application.serviceorder.startexecution.StartServiceOrderExecutionUseCase finishServiceOrderExecutionUseCase) {
        this.repository = repository;
        this.finishServiceOrderExecutionUseCase = finishServiceOrderExecutionUseCase;
    }

    @Override
    public ServiceOrderExecution execute(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        ServiceOrderExecution execution = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
        
        execution.start();
        finishServiceOrderExecutionUseCase.execute(execution.getServiceOrderId());
        
        return repository.save(execution);
    }
}

