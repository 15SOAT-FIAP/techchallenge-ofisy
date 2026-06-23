package br.com.ofisy.adapters.controllers.servicecatalog;

import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "API de Serviços")
public interface ServiceCatalogApi {

    @Operation(summary = "Listar serviços com paginação - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping
    ResponseEntity<Page<ServiceCatalogResponseDTO>> getAll(
            Pageable pageable);

    @Operation(summary = "Obter serviço por ID - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<ServiceCatalogResponseDTO> getById(
            @PathVariable UUID id);

    @Operation(summary = "Obter serviço por nome - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping(params = "name")
    ResponseEntity<ServiceCatalogResponseDTO> getByName(
            @RequestParam String name);

    @Operation(summary = "Obter tempo médio de execução de um serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Tempo médio calculado com sucesso")
    @GetMapping("execution_time_average/{id}")
    ResponseEntity<Double> getExecutionTimeAverage(
            @PathVariable UUID id);

    @Operation(summary = "Criar um novo serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para criação de serviço inválidos")
    @PostMapping
    ResponseEntity<ServiceCatalogResponseDTO> create(
            @RequestBody ServiceCatalogRequestDTO dto);
}
