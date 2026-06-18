package br.com.ofisy.application.serviceorderexecution.identifybyid;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface IdentifyByIdServiceOrderExecutionUseCase {
    ServiceOrderExecution execute(UUID id);
}

