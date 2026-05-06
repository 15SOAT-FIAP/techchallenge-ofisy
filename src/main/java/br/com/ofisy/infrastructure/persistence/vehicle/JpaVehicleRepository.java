package br.com.ofisy.infrastructure.persistence.vehicle;

import br.com.ofisy.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaVehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByCustomerId(UUID customerId);

    Optional<Vehicle> findByLicensePlateValue(String value);
}
