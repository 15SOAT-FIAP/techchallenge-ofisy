package br.com.ofisy.interfaces.api.serviceorder;

import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

@Tag(name = "API de Ordem de Serviço")
public interface ServiceOrderApi {

    @Operation(summary = "Criar uma nova ordem de serviço")
    @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso")
    ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(ServiceOrderRequestDTO request, @Parameter(hidden = true) UserDetails userDetails);

    @Operation(summary = "Iniciar diagnóstico de uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Diagnóstico iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(UUID id);
}
