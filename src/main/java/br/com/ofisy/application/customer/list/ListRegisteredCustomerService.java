package br.com.ofisy.application.customer.list;

import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListRegisteredCustomerService implements ListRegisteredCustomerUseCase {

    private final CustomerRepository customerRepository;

    public ListRegisteredCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Page<Customer> execute(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
}
