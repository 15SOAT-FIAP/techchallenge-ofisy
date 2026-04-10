package br.com.ofisy.infrastructure.persistence.customer;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpa;

    @Override
    public Customer save(Customer customer) {
        return jpa.save(customer);
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Customer> findByCpfCnpj(CpfCnpj cpfCnpj) {
        return jpa.findByCpfCnpjValue(cpfCnpj.getValue());
    }
}
