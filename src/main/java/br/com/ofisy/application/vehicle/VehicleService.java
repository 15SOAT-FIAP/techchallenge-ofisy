package br.com.ofisy.application.vehicle;

import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> listRegisteredVehicles(Pageable pageable) {
        var vehicles = vehicleRepository.findAll(pageable);
        return vehicles.map(VehicleMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listVehiclesFromCustomer(UUID customerId) {
        var vehicles = vehicleRepository.findByCustomerId(customerId);
        return vehicles.stream()
                .map(VehicleMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO identifyVehicleById(UUID id) {
        var vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        return VehicleMapper.toDTO(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO identifyVehicleByLicensePlate(String licensePlate) {
        var vehicle = vehicleRepository.findByLicensePlate(new LicensePlate(licensePlate))
                .orElseThrow(() -> new VehicleLicensePlateNotFoundException(licensePlate));
        return VehicleMapper.toDTO(vehicle);
    }

    @Transactional
    public VehicleResponseDTO registerVehicle(VehicleRequestDTO request) {
        var vehicle = VehicleMapper.toDomain(request);
        if (vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).isPresent()) {
            throw new VehicleAlreadyExistsException(vehicle.getLicensePlate().getValue());
        }
        var registeredVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toDTO(registeredVehicle);
    }
}
