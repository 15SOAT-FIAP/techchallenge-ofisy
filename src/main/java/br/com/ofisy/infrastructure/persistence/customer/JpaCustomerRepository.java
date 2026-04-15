package br.com.ofisy.infrastructure.persistence.customer;

import br.com.ofisy.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByCpfCnpjValue(String value);
}
