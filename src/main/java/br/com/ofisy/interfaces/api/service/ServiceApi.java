package br.com.ofisy.interfaces.api.service;

import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.domain.service.Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "API de Serviços")
public interface ServiceApi {

    @Operation(summary = "Listar serviços com paginação")
    @ApiResponse(responseCode = "200", description = "Serviços listados com sucesso")
    @GetMapping
    ResponseEntity<Page<Service>> getAll(
            Pageable pageable);

    @Operation(summary = "Obter serviço por ID")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<Service> getById(
            @PathVariable UUID id);

    @Operation(summary = "Obter serviço por nome")
    @ApiResponse(responseCode = "200", description = "Serviço listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @GetMapping(params = "name")
    ResponseEntity<Service> getByName(
            @RequestParam String name);

    @Operation(summary = "Obter tempo médio de execução de um serviço")
    @ApiResponse(responseCode = "200", description = "Tempo médio calculado com sucesso")
    @GetMapping("execution_time_average/{id}")
    ResponseEntity<Double> getExecutionTimeAverage(
            @PathVariable UUID id);

    @Operation(summary = "Criar um novo serviço")
    @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para criação de serviço inválidos")
    @PostMapping
    ResponseEntity<Service> create(
            @RequestBody ServiceRequestDTO dto);
}
