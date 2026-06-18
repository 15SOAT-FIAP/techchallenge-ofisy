package br.com.ofisy.application.serviceorderexecution.cancel;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface CancelServiceOrderExecutionUseCase {
    ServiceOrderExecution execute(UUID id);
}

