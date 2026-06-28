package br.com.ofisy.application.serviceorderexecution.list;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListServiceOrderExecutionUseCase {
    Page<ServiceOrderExecution> execute(Pageable pageable);
}

