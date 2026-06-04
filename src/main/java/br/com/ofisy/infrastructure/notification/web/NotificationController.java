package br.com.ofisy.infrastructure.notification.web;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.ports.in.MarkAsReadCommand;
import br.com.ofisy.application.notification.usecases.FindAllNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.FindNotificationByIdUseCase;
import br.com.ofisy.application.notification.usecases.FindUnreadNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.MarkNotificationAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final FindNotificationByIdUseCase findByIdUseCase;
    private final FindAllNotificationsUseCase findAllUseCase;
    private final FindUnreadNotificationsUseCase findUnreadUseCase;
    private final MarkNotificationAsReadUseCase markAsReadUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationDTOMapper.toDTO(findByIdUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> findAll() {
        return ResponseEntity.ok(
            findAllUseCase.execute().stream()
                .map(NotificationDTOMapper::toDTO)
                .toList()
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> findUnread() {
        return ResponseEntity.ok(
            findUnreadUseCase.findUnread().stream()
                .map(NotificationDTOMapper::toDTO)
                .toList()
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(
            NotificationDTOMapper.toDTO(markAsReadUseCase.execute(new MarkAsReadCommand(id)))
        );
    }
}
