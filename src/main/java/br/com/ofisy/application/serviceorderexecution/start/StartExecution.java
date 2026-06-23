package br.com.ofisy.application.serviceorderexecution.start;

import br.com.ofisy.application.serviceorder.startexecution.StartServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class StartExecution implements StartExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;
    private final StartServiceOrderExecutionUseCase startServiceOrderExecutionUseCase;

    public StartExecution(ServiceOrderExecutionRepository repository,
                          StartServiceOrderExecutionUseCase startServiceOrderExecutionUseCase) {
        this.repository = repository;
        this.startServiceOrderExecutionUseCase = startServiceOrderExecutionUseCase;
    }

    @Override
    public ServiceOrderExecution execute(UUID id) {
        ServiceOrderExecution execution = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
        
        execution.start();
        startServiceOrderExecutionUseCase.execute(execution.getServiceOrderId());
        
        return repository.save(execution);
    }
}

