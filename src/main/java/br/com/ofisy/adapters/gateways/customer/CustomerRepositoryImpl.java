package br.com.ofisy.adapters.gateways.customer;

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
        CustomerEntity entity = CustomerMapper.toEntity(customer);
        return CustomerMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        Page<CustomerEntity> entities = jpa.findAll(pageable);
        return entities.map(CustomerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        Optional<CustomerEntity> entityOpt = jpa.findById(id);
        return entityOpt.map(CustomerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByCpfCnpj(CpfCnpj cpfCnpj) {
        Optional<CustomerEntity> entityOpt = jpa.findByCpfCnpj(cpfCnpj.getValue());
        return entityOpt.map(CustomerMapper::toDomain);
    }
}
