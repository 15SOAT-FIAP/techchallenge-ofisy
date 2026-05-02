package br.com.ofisy.interfaces.api.serviceorder;

import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

@Tag(name = "API de Ordem de Serviço")
public interface ServiceOrderApi {

    @Operation(summary = "Criar uma nova ordem de serviço")
    @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso")
    ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(ServiceOrderRequestDTO request, @Parameter(hidden = true) UserDetails userDetails);

    @Operation(summary = "Listar todas as ordens de serviço com status RECEIVED")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de serviço recebidas retornada com sucesso")
    ResponseEntity<Page<ServiceOrderResponseDTO>> listReceived(Pageable pageable);

    @Operation(summary = "Listar todas as ordens de serviço com status FINISHED")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de serviço finalizadas retornada com sucesso")
    ResponseEntity<Page<ServiceOrderResponseDTO>> listFinished(Pageable pageable);

    @Operation(summary = "Acompanhar o status da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Status da ordem de serviço retornado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    ResponseEntity<ServiceOrderStatusResponseDTO> getStatusById(UUID id);

    @Operation(summary = "Iniciar diagnóstico de uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Diagnóstico iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(UUID id);

    @Operation(summary = "Entregar carro ao cliente para uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Carro entregue ao cliente com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> deliverToCustomerServiceOrder(UUID id);

    @Operation(summary = "Cancelar uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Ordem de serviço cancelada com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> cancelServiceOrder(UUID id);

    @Operation(summary = "Gerar orçamento para uma ordem de serviço")
    @ApiResponse(responseCode = "201", description = "Orçamento gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    ResponseEntity<QuoteResponseDTO> generateQuote(UUID id, CreateQuoteRequestDTO request);

    @Operation(summary = "Aprovar orçamento para uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento aprovado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    ResponseEntity<QuoteResponseDTO> approveQuote(UUID id);

    @Operation(summary = "Reprovar orçamento para uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento reprovado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    ResponseEntity<QuoteResponseDTO> reproveQuote(UUID id, ReproveQuoteRequestDTO reproveQuoteRequestDTO);
}
