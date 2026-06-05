package br.com.ofisy.adapters.gateways.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByCpfCnpj(String value);
}
