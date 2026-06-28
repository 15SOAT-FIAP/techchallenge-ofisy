package br.com.ofisy.application.serviceorderexecution.listbystatus;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListByStatusServiceOrderExecutionService implements ListByStatusServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public ListByStatusServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ServiceOrderExecution> execute(String status, Pageable pageable) {
        ServiceOrderExecutionStatus executionStatus =
                ServiceOrderExecutionStatus.from(status);

        return repository.findByStatus(executionStatus, pageable);
    }
}

