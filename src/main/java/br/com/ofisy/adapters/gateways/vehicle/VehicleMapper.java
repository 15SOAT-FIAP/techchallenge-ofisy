package br.com.ofisy.adapters.gateways.vehicle;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VehicleMapper {

    public static Vehicle toDomain(VehicleEntity entity) {
        return Vehicle.reconstruct(
                entity.getId(),
                entity.getCustomerId(),
                new LicensePlate(entity.getLicensePlate()),
                entity.getModel(),
                entity.getBrand(),
                entity.getColor(),
                entity.getYear(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static VehicleEntity toEntity(Vehicle vehicle) {
        return VehicleEntity.builder()
                .id(vehicle.getId())
                .customerId(vehicle.getCustomerId())
                .licensePlate(vehicle.getLicensePlate().getValue())
                .model(vehicle.getModel())
                .brand(vehicle.getBrand())
                .color(vehicle.getColor())
                .year(vehicle.getYear())
                .description(vehicle.getDescription())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}