package br.com.ofisy.application.customer.identifybycpfcnpj;

import br.com.ofisy.domain.customer.Customer;

public interface IdentifyByCpfCnpjCustomerUseCase {

    Customer execute(String cpfCnpj);
}
