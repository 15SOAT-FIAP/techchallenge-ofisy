package br.com.ofisy.infrastructure.persistence.service;

import br.com.ofisy.domain.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByName(String name);
}
