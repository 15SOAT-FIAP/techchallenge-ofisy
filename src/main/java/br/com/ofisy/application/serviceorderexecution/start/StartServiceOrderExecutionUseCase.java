package br.com.ofisy.application.serviceorderexecution.start;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface StartServiceOrderExecutionUseCase {
    ServiceOrderExecution execute(UUID id);
}

