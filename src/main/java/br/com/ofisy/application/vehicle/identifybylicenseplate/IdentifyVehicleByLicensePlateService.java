package br.com.ofisy.application.vehicle.identifybylicenseplate;

import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentifyVehicleByLicensePlateService implements IdentifyVehicleByLicensePlateUseCase {

    private final VehicleRepository vehicleRepository;

    public IdentifyVehicleByLicensePlateService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle execute(String licensePlate) {
        return vehicleRepository.findByLicensePlate(new LicensePlate(licensePlate))
                .orElseThrow(() -> new VehicleLicensePlateNotFoundException(licensePlate));
    }
}