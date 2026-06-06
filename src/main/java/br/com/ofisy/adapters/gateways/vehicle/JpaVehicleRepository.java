package br.com.ofisy.adapters.gateways.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaVehicleRepository extends JpaRepository<VehicleEntity, UUID> {

    List<VehicleEntity> findByCustomerId(UUID customerId);

    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
}