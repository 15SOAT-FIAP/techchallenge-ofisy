package br.com.ofisy.interfaces.api.vehicle;

import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController implements VehicleApi {

    private final VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<Page<VehicleResponseDTO>> getAllVehicles(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(vehicleService.listRegisteredVehicles(pageable));
    }

    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<List<VehicleResponseDTO>> getVehicleFromCustomerId(
            @PathVariable UUID customerId) {

        return ResponseEntity.ok(vehicleService.listVehiclesFromCustomer(customerId));
    }

    @GetMapping(params = "licensePlate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> getVehicleByLicensePlate(
            @RequestParam(required = false) String licensePlate) {

        return ResponseEntity.ok(vehicleService.identifyVehicleByLicensePlate(licensePlate));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(vehicleService.identifyVehicleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> registerVehicle(
    @Valid @RequestBody VehicleRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.registerVehicle(request));
    }
}
