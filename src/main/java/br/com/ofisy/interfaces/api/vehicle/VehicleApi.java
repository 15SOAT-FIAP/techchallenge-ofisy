package br.com.ofisy.interfaces.api.vehicle;


import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Tag(name = "API de Veiculos")
public interface VehicleApi {

    @Operation(summary = "Listar veículos")
    @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso")
    @GetMapping
    ResponseEntity<Page<VehicleResponseDTO>> getAllVehicles(Pageable pageable);

    @Operation(summary = "Listar veículos por cliente")
    @ApiResponse(responseCode = "200", description = "Lista de veículos do cliente retornada com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @GetMapping("/customers/{customerId}")
    ResponseEntity<List<VehicleResponseDTO>> getVehicleFromCustomerId(UUID customerId);

    @Operation(summary = "Obter veículo por placa")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    @GetMapping(params = "licensePlate")
    ResponseEntity<VehicleResponseDTO> getVehicleByLicensePlate(String licensePlate);

    @Operation(summary = "Obter veículo por ID")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<VehicleResponseDTO> getVehicleById(UUID id);

    @Operation(summary = "Registrar um novo veículo")
    @ApiResponse(responseCode = "201", description = "Veículo registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados do veículo inválidos")
    @PostMapping
    ResponseEntity<VehicleResponseDTO> registerVehicle(VehicleRequestDTO request);
}
