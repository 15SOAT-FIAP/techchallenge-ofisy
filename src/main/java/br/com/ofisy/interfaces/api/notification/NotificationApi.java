package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
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

    @Operation(summary = "Buscar notificação por ID - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Notificação encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    @GetMapping("/{id}")
    ResponseEntity<NotificationResponseDTO> findById(@PathVariable UUID id);

    @Operation(summary = "Listar todas as notificações - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Notificações listadas com sucesso")
    @GetMapping
    ResponseEntity<List<NotificationResponseDTO>> findAll();

    @Operation(summary = "Listar notificações não lidas - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Notificações não lidas listadas com sucesso")
    @GetMapping("/unread")
    ResponseEntity<List<NotificationResponseDTO>> findUnread();

    @Operation(summary = "Marcar notificação como lida - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    @PatchMapping("/{id}/read")
    ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id);
}
