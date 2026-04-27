package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> findAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> findUnread() {
        return ResponseEntity.ok(notificationService.findUnread());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id) {
        NotificationResponseDTO result = notificationService.markAsRead(id);
        return ResponseEntity.ok(result);
    }
}
