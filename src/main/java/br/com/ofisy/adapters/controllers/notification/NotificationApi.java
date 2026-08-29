package br.com.ofisy.adapters.controllers.notification;

import br.com.ofisy.adapters.controllers.notification.dto.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Tag(name = "API de Notificações")
public interface NotificationApi {

    @Operation(summary = "Buscar notificação de estoque por ID - Roles autorizadas: (STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Notificação encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada ou não é do tipo estoque")
    @GetMapping("/stock/{id}")
    ResponseEntity<NotificationResponseDTO> findStockById(@PathVariable UUID id);

    @Operation(summary = "Buscar notificação de ordem de serviço por ID - Requer autenticação de staff, sem restrição de papel")
    @ApiResponse(responseCode = "200", description = "Notificação encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada ou não é do tipo ordem de serviço")
    @GetMapping("/service-orders/{id}")
    ResponseEntity<NotificationResponseDTO> findServiceOrderById(@PathVariable UUID id);

    @Operation(summary = "Listar notificações de estoque - Roles autorizadas: (STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Notificações de estoque listadas com sucesso")
    @GetMapping("/stock")
    ResponseEntity<List<NotificationResponseDTO>> findAllStock();

    @Operation(summary = "Listar notificações de estoque não lidas - Roles autorizadas: (STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Notificações de estoque não lidas listadas com sucesso")
    @GetMapping("/stock/unread")
    ResponseEntity<List<NotificationResponseDTO>> findUnreadStock();

    @Operation(summary = "Listar notificações de ordem de serviço - Requer autenticação de staff, sem restrição de papel")
    @ApiResponse(responseCode = "200", description = "Notificações de ordem de serviço listadas com sucesso")
    @GetMapping("/service-orders")
    ResponseEntity<List<NotificationResponseDTO>> findAllServiceOrders();

    @Operation(summary = "Listar notificações de ordem de serviço não lidas - Requer autenticação de staff, sem restrição de papel")
    @ApiResponse(responseCode = "200", description = "Notificações de ordem de serviço não lidas listadas com sucesso")
    @GetMapping("/service-orders/unread")
    ResponseEntity<List<NotificationResponseDTO>> findUnreadServiceOrders();

    @Operation(summary = "Marcar notificação de estoque como lida - Roles autorizadas: (STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada ou não é do tipo estoque")
    @PatchMapping("/stock/{id}/read")
    ResponseEntity<NotificationResponseDTO> markStockAsRead(@PathVariable UUID id);

    @Operation(summary = "Marcar notificação de ordem de serviço como lida - Requer autenticação de staff, sem restrição de papel")
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada ou não é do tipo ordem de serviço")
    @PatchMapping("/service-orders/{id}/read")
    ResponseEntity<NotificationResponseDTO> markServiceOrderAsRead(@PathVariable UUID id);
}
