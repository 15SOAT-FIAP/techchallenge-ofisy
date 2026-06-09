package br.com.ofisy.application.vehicle.listall;

import br.com.ofisy.domain.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListRegisteredVehiclesUseCase {

    Page<Vehicle> execute(Pageable pageable);
}