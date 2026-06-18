package br.com.ofisy.application.serviceorderexecution.listbyservicecatalogid;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListByServiceCatalogIdServiceOrderExecutionUseCase {
    Page<ServiceOrderExecution> execute(UUID serviceCatalogId, Pageable pageable);
}

