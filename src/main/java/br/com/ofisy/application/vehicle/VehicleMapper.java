package br.com.ofisy.application.vehicle;

import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VehicleMapper {

    public static VehicleResponseDTO toDTO(Vehicle vehicle) {
        return new VehicleResponseDTO(
                vehicle.getId(),
                vehicle.getCustomerId(),
                vehicle.getLicensePlate().getValue(),
                vehicle.getModel(),
                vehicle.getBrand(),
                vehicle.getColor(),
                vehicle.getYear(),
                vehicle.getDescription(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    public static Vehicle toDomain(VehicleRequestDTO request) {
        return Vehicle.create(
                request.customerId(),
                new LicensePlate(request.licensePlate()),
                request.model(),
                request.brand(),
                request.color(),
                request.year(),
                request.description()
        );
    }
}
