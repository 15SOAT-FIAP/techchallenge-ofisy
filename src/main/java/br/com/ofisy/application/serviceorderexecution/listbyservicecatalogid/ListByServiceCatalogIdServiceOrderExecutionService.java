package br.com.ofisy.application.serviceorderexecution.listbyservicecatalogid;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListByServiceCatalogIdServiceOrderExecutionService implements ListByServiceCatalogIdServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public ListByServiceCatalogIdServiceOrderExecutionService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ServiceOrderExecution> execute(UUID serviceCatalogId, Pageable pageable) {
        if (serviceCatalogId == null) {
            throw new IllegalArgumentException("Service Catalog ID não pode ser nulo");
        }
        return repository.findByServiceCatalogId(serviceCatalogId, pageable);
    }
}

