package br.com.ofisy.application.vehicle.register;

import br.com.ofisy.domain.vehicle.Vehicle;

import java.util.UUID;

public interface RegisterVehicleUseCase {

    Vehicle execute(RegisterVehicleCommand cmd);

    record RegisterVehicleCommand(
            UUID customerId,
            String licensePlate,
            String model,
            String brand,
            String color,
            Integer year,
            String description
    ) {}
}