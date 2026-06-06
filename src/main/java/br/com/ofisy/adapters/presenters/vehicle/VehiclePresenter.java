package br.com.ofisy.adapters.presenters.vehicle;

import br.com.ofisy.adapters.controllers.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VehiclePresenter {

    public static VehicleResponseDTO present(Vehicle vehicle) {
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
}