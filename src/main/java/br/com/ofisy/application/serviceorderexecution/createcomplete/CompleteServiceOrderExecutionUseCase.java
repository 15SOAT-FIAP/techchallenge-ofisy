package br.com.ofisy.application.serviceorderexecution.createcomplete;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface CompleteServiceOrderExecutionUseCase {
    ServiceOrderExecution execute(UUID id);
}

