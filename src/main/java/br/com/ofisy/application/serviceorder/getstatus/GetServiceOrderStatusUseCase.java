package br.com.ofisy.application.serviceorder.getstatus;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface GetServiceOrderStatusUseCase {

    ServiceOrder execute(UUID id);
}