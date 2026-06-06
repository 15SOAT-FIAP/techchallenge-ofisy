package br.com.ofisy.application.vehicle.listbycustomer;

import br.com.ofisy.domain.vehicle.Vehicle;

import java.util.List;
import java.util.UUID;

public interface ListVehiclesByCustomerUseCase {

    List<Vehicle> execute(UUID customerId);
}