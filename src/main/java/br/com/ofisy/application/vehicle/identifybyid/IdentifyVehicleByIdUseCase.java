package br.com.ofisy.application.vehicle.identifybyid;

import br.com.ofisy.domain.vehicle.Vehicle;

import java.util.UUID;

public interface IdentifyVehicleByIdUseCase {

    Vehicle execute(UUID id);
}