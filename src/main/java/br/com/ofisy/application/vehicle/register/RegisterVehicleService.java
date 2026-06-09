package br.com.ofisy.application.vehicle.register;

import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    public RegisterVehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle execute(RegisterVehicleCommand cmd) {
        Vehicle vehicle = Vehicle.create(
                cmd.customerId(),
                new LicensePlate(cmd.licensePlate()),
                cmd.model(),
                cmd.brand(),
                cmd.color(),
                cmd.year(),
                cmd.description()
        );
        if (vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).isPresent()) {
            throw new VehicleAlreadyExistsException(vehicle.getLicensePlate().getValue());
        }
        return vehicleRepository.save(vehicle);
    }
}