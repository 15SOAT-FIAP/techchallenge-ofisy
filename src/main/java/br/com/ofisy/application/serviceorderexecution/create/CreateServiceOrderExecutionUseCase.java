package br.com.ofisy.application.serviceorderexecution.create;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

import java.util.UUID;

public interface CreateServiceOrderExecutionUseCase {

    ServiceOrderExecution execute(CreateServiceOrderExecutionCommand cmd);

    record CreateServiceOrderExecutionCommand(
            UUID serviceCatalogId,
            UUID serviceOrderId
    ) {}
}

