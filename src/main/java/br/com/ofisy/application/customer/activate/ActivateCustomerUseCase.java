package br.com.ofisy.application.customer.activate;

import br.com.ofisy.domain.customer.Customer;

import java.util.UUID;

public interface ActivateCustomerUseCase {
    Customer execute(UUID id);
}