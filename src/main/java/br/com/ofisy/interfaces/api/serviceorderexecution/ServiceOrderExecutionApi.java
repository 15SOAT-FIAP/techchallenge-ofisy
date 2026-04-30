package br.com.ofisy.interfaces.api.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "API de Serviços de Ordem de Serviço")
public interface ServiceOrderExecutionApi {

    @Operation(summary = "Listar serviços de ordem de serviço com paginação - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping
    ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getAll(
            Pageable pageable);

    @Operation(summary = "Obter serviço de ordem de serviço por ID - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<ServiceOrderExecutionResponseDTO> getById(
            @PathVariable UUID id);

    @Operation(summary = "Listar serviços de ordem de serviço por ID do serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping(params = "serviceCatalogId")
    ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceCatalogId(
            @RequestParam UUID serviceCatalogId,
            Pageable pageable);

    @Operation(summary = "Listar serviços de ordem de serviço por status - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping(params = "status")
    ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByStatus(
            @RequestParam String status,
            Pageable pageable);

    @Operation(summary = "Listar serviços de ordem de serviço por ID da ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping("/service_order/{id}")
    ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceOrderId(
            @PathVariable UUID id,
            Pageable pageable);

    @Operation(summary = "Criar um novo serviço de ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para criação inválidos")
    @PostMapping
    ResponseEntity<ServiceOrderExecutionResponseDTO> create(
            @RequestBody ServiceOrderExecutionRequestDTO requestDTO);

    @Operation(summary = "Completar um serviço de ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço completado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/complete")
    ResponseEntity<ServiceOrderExecutionResponseDTO> complete(
            @PathVariable UUID id);

    @Operation(summary = "Cancelar um serviço de ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço cancelado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/cancel")
    ResponseEntity<ServiceOrderExecutionResponseDTO> cancel(
            @PathVariable UUID id);

    @Operation(summary = "Iniciar um serviço de ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Serviço iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/start")
    ResponseEntity<ServiceOrderExecutionResponseDTO> start(
            @PathVariable UUID id);
}
