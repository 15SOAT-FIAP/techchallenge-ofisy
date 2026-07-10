package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.quote.dto.UpdateQuoteRequestDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.*;
import br.com.ofisy.adapters.controllers.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.adapters.controllers.quote.dto.QuoteResponseDTO;
import br.com.ofisy.adapters.controllers.quote.dto.ReproveQuoteRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Tag(name = "API de Ordem de Serviço")
public interface ServiceOrderApi {

    @Operation(summary = "Criar uma nova ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso")
    ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(ServiceOrderRequestDTO request, @Parameter(hidden = true) UserDetails userDetails);

    @Operation(summary = "Criar uma nova ordem de serviço completa com itens e serviços - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "201", description = "Ordem de serviço com itens/serviços criada com sucesso")
    ResponseEntity<ServiceOrderResponseDTO> receiveCompleteServiceOrder(CreateCompleteServiceOrderRequestDTO request, UserDetails user);

    @Operation(summary = "Listar todas as ordens de serviço com status RECEIVED - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de serviço recebidas retornada com sucesso")
    ResponseEntity<Page<ServiceOrderResponseDTO>> listReceived(Pageable pageable);

    @Operation(summary = "Listar todas as ordens de serviço com status FINISHED - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de serviço finalizadas retornada com sucesso")
    ResponseEntity<Page<ServiceOrderResponseDTO>> listFinished(Pageable pageable);

    @Operation(summary = "Listar ordens de serviço ativas ordenadas por prioridade de status - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de serviço ativas retornada com sucesso")
    ResponseEntity<Page<ServiceOrderResponseDTO>> listActive(Pageable pageable);

    @Operation(summary = "Acompanhar o status da ordem de serviço - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Status da ordem de serviço retornado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    ResponseEntity<ServiceOrderStatusResponseDTO> getStatusById(UUID id);

    @Operation(summary = "Iniciar diagnóstico de uma ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Diagnóstico iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(UUID id);

    @Operation(summary = "Entregar carro ao cliente para uma ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Carro entregue ao cliente com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> deliverToCustomerServiceOrder(UUID id);

    @Operation(summary = "Cancelar uma ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Ordem de serviço cancelada com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> cancelServiceOrder(UUID id);

    @Operation(summary = "Gerar orçamento para uma ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "201", description = "Orçamento gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    ResponseEntity<QuoteResponseDTO> generateQuote(UUID id, CreateQuoteRequestDTO request);

    @Operation(summary = "Editar orçamento para uma ordem de serviço - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "201", description = "Orçamento editado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    ResponseEntity<QuoteResponseDTO> updateQuote(UUID id, UpdateQuoteRequestDTO request);

    @Operation(summary = "Aprovar orçamento para uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento aprovado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<QuoteResponseDTO> approveQuote(UUID id);

    @Operation(summary = "Reprovar orçamento para uma ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento reprovado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<QuoteResponseDTO> reproveQuote(UUID id, ReproveQuoteRequestDTO reproveQuoteRequestDTO);

    @Operation(summary = "Submeter orçamento para aprovação sem edição - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Orçamento submetido para aprovação com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado para a OS informada")
    @ApiResponse(responseCode = "409", description = "Transição de estado inválida")
    ResponseEntity<ServiceOrderResponseDTO> submitQuoteForApproval(UUID id);

    @Operation(summary = "Lista orçamentos existentes para a OS informada - Roles autorizadas: (ADMIN, ATTENDANT, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Orçamentos encontrados para OS")
    @ApiResponse(responseCode = "404", description = "Orçamentos não encontrados para a OS informada")
    ResponseEntity<List<QuoteResponseDTO>> listQuotesByServiceOrder(UUID id);
}
