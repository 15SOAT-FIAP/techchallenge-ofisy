package br.com.ofisy.application.vehicle.identifybylicenseplate;

import br.com.ofisy.domain.vehicle.Vehicle;

public interface IdentifyVehicleByLicensePlateUseCase {

    Vehicle execute(String licensePlate);
}