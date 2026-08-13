package br.com.ofisy.application.customer.deactivate;

import br.com.ofisy.domain.customer.Customer;

import java.util.UUID;

public interface DeactivateCustomerUseCase {
    Customer execute(UUID id);
}