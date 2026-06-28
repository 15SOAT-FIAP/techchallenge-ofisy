package br.com.ofisy.application.serviceorderexecution.list;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListServiceOrderExecutionService implements ListServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public ListServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ServiceOrderExecution> execute(Pageable pageable) {
        return repository.findAll(pageable);
    }
}

