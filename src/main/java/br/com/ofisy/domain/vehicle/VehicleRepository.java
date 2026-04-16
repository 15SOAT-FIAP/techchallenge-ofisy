package br.com.ofisy.domain.vehicle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Page<Vehicle> findAll(Pageable pageable);

    List<Vehicle> findByCustomerId(UUID customerId);

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate);
}
