package br.com.ofisy.interfaces.api.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
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

    @Operation(summary = "Listar serviços de ordem de serviço com paginação")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping
    ResponseEntity<Page<ServiceOrderExecution>> getAll(
            Pageable pageable);

    @Operation(summary = "Obter serviço de ordem de serviço por ID")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<ServiceOrderExecution> getById(
            @PathVariable UUID id);

    @Operation(summary = "Listar serviços de ordem de serviço por ID do serviço")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping(params = "serviceCatalogId")
    ResponseEntity<Page<ServiceOrderExecution>> getByServiceCatalogId(
            @RequestParam UUID serviceCatalogId,
            Pageable pageable);

    @Operation(summary = "Listar serviços de ordem de serviço por status")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping(params = "status")
    ResponseEntity<Page<ServiceOrderExecution>> getByStatus(
            @RequestParam String status,
            Pageable pageable);

    @Operation(summary = "Listar serviços de ordem de serviço por ID da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping("/service_order/{id}")
    ResponseEntity<Page<ServiceOrderExecution>> getByServiceOrderId(
            @PathVariable UUID id,
            Pageable pageable);

    @Operation(summary = "Criar um novo serviço de ordem de serviço")
    @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para criação inválidos")
    @PostMapping
    ResponseEntity<ServiceOrderExecution> create(
            @RequestBody ServiceOrderExecutionRequestDTO requestDTO);

    @Operation(summary = "Completar um serviço de ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Serviço completado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/complete")
    ResponseEntity<ServiceOrderExecution> complete(
            @PathVariable UUID id);

    @Operation(summary = "Cancelar um serviço de ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Serviço cancelado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/cancel")
    ResponseEntity<ServiceOrderExecution> cancel(
            @PathVariable UUID id);

    @Operation(summary = "Iniciar um serviço de ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Serviço iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @PatchMapping("/{id}/start")
    ResponseEntity<ServiceOrderExecution> start(
            @PathVariable UUID id);
}
