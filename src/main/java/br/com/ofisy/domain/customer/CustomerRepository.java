package br.com.ofisy.domain.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Page<Customer> findAll(Pageable pageable);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByCpfCnpj(CpfCnpj cpfCnpj);
}
