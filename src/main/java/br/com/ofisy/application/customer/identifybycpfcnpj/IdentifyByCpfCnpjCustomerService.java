package br.com.ofisy.application.customer.identifybycpfcnpj;

import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentifyByCpfCnpjCustomerService implements IdentifyByCpfCnpjCustomerUseCase{

    private final CustomerRepository customerRepository;

    public IdentifyByCpfCnpjCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer execute(String cpfCnpj) {
        return customerRepository.findByCpfCnpj(new CpfCnpj(cpfCnpj))
                .orElseThrow(() -> new CustomerCpfCnpjNotFoundException(cpfCnpj));
    }
}
