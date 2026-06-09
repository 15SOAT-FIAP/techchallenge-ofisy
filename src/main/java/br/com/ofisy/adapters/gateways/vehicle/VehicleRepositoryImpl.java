package br.com.ofisy.adapters.gateways.vehicle;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepository {

    private final JpaVehicleRepository jpa;

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = VehicleMapper.toEntity(vehicle);
        return VehicleMapper.toDomain(jpa.save(entity));
    }

    @Override
    public Page<Vehicle> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(VehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findByCustomerId(UUID customerId) {
        return jpa.findByCustomerId(customerId).stream()
                .map(VehicleMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpa.findById(id).map(VehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate) {
        return jpa.findByLicensePlate(licensePlate.getValue()).map(VehicleMapper::toDomain);
    }
}