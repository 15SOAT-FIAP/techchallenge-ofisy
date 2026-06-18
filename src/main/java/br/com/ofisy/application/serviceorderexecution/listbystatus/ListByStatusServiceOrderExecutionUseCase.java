package br.com.ofisy.application.serviceorderexecution.listbystatus;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListByStatusServiceOrderExecutionUseCase {
    Page<ServiceOrderExecution> execute(String status, Pageable pageable);
}

