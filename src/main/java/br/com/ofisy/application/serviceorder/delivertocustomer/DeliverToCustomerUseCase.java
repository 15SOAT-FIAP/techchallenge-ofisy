package br.com.ofisy.application.serviceorder.delivertocustomer;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface DeliverToCustomerUseCase {

    ServiceOrder execute(UUID id);
}