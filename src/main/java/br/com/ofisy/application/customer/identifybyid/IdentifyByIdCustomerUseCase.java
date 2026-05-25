package br.com.ofisy.application.customer.identifybyid;

import br.com.ofisy.domain.customer.Customer;

import java.util.UUID;

public interface IdentifyByIdCustomerUseCase {

    Customer execute(UUID id);
}
