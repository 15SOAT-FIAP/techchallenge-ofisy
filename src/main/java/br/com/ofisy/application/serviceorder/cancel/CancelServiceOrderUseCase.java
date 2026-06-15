package br.com.ofisy.application.serviceorder.cancel;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface CancelServiceOrderUseCase {

    ServiceOrder execute(UUID id);
}