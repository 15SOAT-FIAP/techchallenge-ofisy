package br.com.ofisy.application.vehicle.register;

import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;

    public RegisterVehicleService(VehicleRepository vehicleRepository,
                                  IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase) {
        this.vehicleRepository = vehicleRepository;
        this.identifyByIdCustomerUseCase = identifyByIdCustomerUseCase;
    }

    @Override
    public Vehicle execute(RegisterVehicleCommand cmd) {
        Customer customer = identifyByIdCustomerUseCase.execute(cmd.customerId());
        customer.validateIsActive();

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