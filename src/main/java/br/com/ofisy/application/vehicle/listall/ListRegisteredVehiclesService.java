package br.com.ofisy.application.vehicle.listall;

import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListRegisteredVehiclesService implements ListRegisteredVehiclesUseCase {

    private final VehicleRepository vehicleRepository;

    public ListRegisteredVehiclesService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Page<Vehicle> execute(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }
}