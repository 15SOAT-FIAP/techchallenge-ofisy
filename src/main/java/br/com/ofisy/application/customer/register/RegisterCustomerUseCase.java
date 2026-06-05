package br.com.ofisy.application.customer.register;

import br.com.ofisy.domain.customer.Customer;

public interface RegisterCustomerUseCase {

    Customer execute(RegisterCustomerCommand cmd);

    record RegisterCustomerCommand(
            String cpfCnpj,
            String name,
            String email,
            String phone
    ) {}
}
