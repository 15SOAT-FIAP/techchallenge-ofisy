package br.com.ofisy.application.customer.deactivate;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeactivateCustomerService implements DeactivateCustomerUseCase {

    private final CustomerRepository repository;

    public DeactivateCustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer execute(UUID id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.deactivate();
        return repository.save(customer);
    }
}