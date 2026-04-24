package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.dto.CreateNotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notificações")
public interface NotificationApi {

    @Operation(summary = "Criar uma nova notificação")
    @ApiResponse(responseCode = "201", description = "Notificação criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da notificação inválidos")
    @PostMapping
    ResponseEntity<NotificationResponseDTO> create(@Valid @RequestBody CreateNotificationRequestDTO request);

    @Operation(summary = "Listar todas as notificações")
    @ApiResponse(responseCode = "200", description = "Notificações listadas com sucesso")
    @GetMapping
    ResponseEntity<List<NotificationResponseDTO>> findAll();

    @Operation(summary = "Listar notificações não lidas")
    @ApiResponse(responseCode = "200", description = "Notificações não lidas listadas com sucesso")
    @GetMapping("/unread")
    ResponseEntity<List<NotificationResponseDTO>> findUnread();

    @Operation(summary = "Marcar notificação como lida")
    @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    @PutMapping("/{id}/read")
    ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id);
}
