package br.com.ofisy.application.vehicle.listbycustomer;

import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListVehiclesByCustomerService implements ListVehiclesByCustomerUseCase {

    private final VehicleRepository vehicleRepository;

    public ListVehiclesByCustomerService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public List<Vehicle> execute(UUID customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }
}