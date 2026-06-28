package br.com.ofisy.application.serviceorderexecution.listbyserviceorderid;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListByServiceOrderIdServiceOrderExecutionUseCase {
    Page<ServiceOrderExecution> execute(UUID serviceOrderId, Pageable pageable);
}

