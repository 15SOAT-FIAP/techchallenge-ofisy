package br.com.ofisy.domain.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    void save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByCpfCnpj(CpfCnpj cpfCnpj);
}
