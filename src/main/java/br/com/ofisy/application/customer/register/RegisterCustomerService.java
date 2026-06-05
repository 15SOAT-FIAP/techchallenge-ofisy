package br.com.ofisy.application.customer.register;

import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterCustomerService implements RegisterCustomerUseCase{

    private final CustomerRepository customerRepository;

    public RegisterCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer execute(RegisterCustomerCommand cmd) {
        Customer customer = Customer.create(
                new CpfCnpj(cmd.cpfCnpj()),
                cmd.name(),
                cmd.email(),
                cmd.phone()
        );
        if (customerRepository.findByCpfCnpj(customer.getCpfCnpj()).isPresent()) {
            throw new CustomerAlreadyExistsException(customer.getCpfCnpj().getValue());
        }

        return customerRepository.save(customer);
    }
}
