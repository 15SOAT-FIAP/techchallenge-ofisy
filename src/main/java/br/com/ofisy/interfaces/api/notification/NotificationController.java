package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
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
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> listAll() {
        return ResponseEntity.ok(notificationService.listAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> listUnread() {
        return ResponseEntity.ok(notificationService.listUnread());
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> send(@Valid @RequestBody NotificationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
