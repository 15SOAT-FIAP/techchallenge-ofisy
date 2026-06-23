package br.com.ofisy.application.serviceorderexecution.complete;

import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase;
import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CompleteServiceOrderExecutionService implements CompleteServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;
    private final FinishServiceOrderUseCase finishServiceOrderUseCase;

    public CompleteServiceOrderExecutionService(ServiceOrderExecutionRepository repository,
                                               FinishServiceOrderUseCase finishServiceOrderUseCase) {
        this.repository = repository;
        this.finishServiceOrderUseCase = finishServiceOrderUseCase;
    }

    @Override
    public ServiceOrderExecution execute(UUID id) {
        ServiceOrderExecution execution = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));

        execution.complete();
        ServiceOrderExecution savedExecution = repository.save(execution);

        if (allServicesFinished(savedExecution.getServiceOrderId())) {
            finishServiceOrderUseCase.execute(savedExecution.getServiceOrderId());
        }

        return savedExecution;
    }

    private boolean allServicesFinished(UUID serviceOrderId) {
        long totalServices = repository.countByServiceOrderId(serviceOrderId);
        long totalServicesCompleted = repository.countByServiceOrderIdAndStatus(serviceOrderId, ServiceOrderExecutionStatus.COMPLETED);

        return totalServices > 0 && totalServices == totalServicesCompleted;
    }
}

