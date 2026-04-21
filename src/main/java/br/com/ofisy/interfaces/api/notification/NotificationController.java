package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "API para gerenciamento de notificações do sistema")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Listar todas as notificações", description = "Retorna a lista completa de notificações cadastradas no sistema")
    @ApiResponse(responseCode = "200", description = "Lista de notificações retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> listAll() {
        return ResponseEntity.ok(notificationService.listAll());
    }

    @Operation(summary = "Listar notificações não lidas", description = "Retorna a lista de notificações que ainda não foram lidas")
    @ApiResponse(responseCode = "200", description = "Lista de notificações não lidas retornada com sucesso")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> listUnread() {
        return ResponseEntity.ok(notificationService.listUnread());
    }

    @Operation(summary = "Enviar notificação", description = "Cria e envia uma nova notificação para o sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificação criada e enviada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos na requisição")
    })
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> send(@Valid @RequestBody NotificationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    @Operation(summary = "Marcar notificação como lida", description = "Atualiza o status de uma notificação específica para lida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação marcada como lida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o ID fornecido")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @Parameter(description = "ID da notificação a ser marcada como lida", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
