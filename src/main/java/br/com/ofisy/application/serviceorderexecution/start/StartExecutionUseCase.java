package br.com.ofisy.application.serviceorderexecution.start;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface StartExecutionUseCase {
    ServiceOrderExecution execute(UUID id);
}

