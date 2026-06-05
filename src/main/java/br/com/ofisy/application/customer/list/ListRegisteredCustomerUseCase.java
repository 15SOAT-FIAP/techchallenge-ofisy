package br.com.ofisy.application.customer.list;

import br.com.ofisy.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListRegisteredCustomerUseCase {

    Page<Customer> execute(Pageable pageable);
}
