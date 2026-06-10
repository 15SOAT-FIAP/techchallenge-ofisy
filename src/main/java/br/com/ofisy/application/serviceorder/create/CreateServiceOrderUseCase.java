package br.com.ofisy.application.serviceorder.create;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface CreateServiceOrderUseCase {

    ServiceOrder execute(CreateServiceOrderCommand cmd);

    record CreateServiceOrderCommand(
            UUID vehicleId,
            UUID customerId,
            String report,
            String createdByEmail
    ) {}
}