package br.com.ofisy.adapters.controllers.vehicle;

import br.com.ofisy.adapters.controllers.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.adapters.controllers.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.adapters.presenters.vehicle.VehiclePresenter;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.application.vehicle.identifybylicenseplate.IdentifyVehicleByLicensePlateUseCase;
import br.com.ofisy.application.vehicle.listall.ListRegisteredVehiclesUseCase;
import br.com.ofisy.application.vehicle.listbycustomer.ListVehiclesByCustomerUseCase;
import br.com.ofisy.application.vehicle.register.RegisterVehicleUseCase;
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

    private final RegisterVehicleUseCase registerVehicleUseCase;
    private final ListRegisteredVehiclesUseCase listRegisteredVehiclesUseCase;
    private final ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase;
    private final IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    private final IdentifyVehicleByLicensePlateUseCase identifyVehicleByLicensePlateUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<Page<VehicleResponseDTO>> getAllVehicles(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listRegisteredVehiclesUseCase.execute(pageable)
                .map(VehiclePresenter::present));
    }

    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<List<VehicleResponseDTO>> getVehicleFromCustomerId(
            @PathVariable UUID customerId) {

        return ResponseEntity.ok(listVehiclesByCustomerUseCase.execute(customerId).stream()
                .map(VehiclePresenter::present)
                .toList());
    }

    @GetMapping(params = "licensePlate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> getVehicleByLicensePlate(
            @RequestParam(required = false) String licensePlate) {

        return ResponseEntity.ok(VehiclePresenter.present(
                identifyVehicleByLicensePlateUseCase.execute(licensePlate)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(VehiclePresenter.present(
                identifyVehicleByIdUseCase.execute(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<VehicleResponseDTO> registerVehicle(
            @Valid @RequestBody VehicleRequestDTO request) {

        RegisterVehicleUseCase.RegisterVehicleCommand cmd = new RegisterVehicleUseCase.RegisterVehicleCommand(
                request.customerId(),
                request.licensePlate(),
                request.model(),
                request.brand(),
                request.color(),
                request.year(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VehiclePresenter.present(registerVehicleUseCase.execute(cmd)));
    }
}