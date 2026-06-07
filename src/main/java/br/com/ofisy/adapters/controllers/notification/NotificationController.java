package br.com.ofisy.adapters.controllers.notification;

import br.com.ofisy.adapters.controllers.notification.dto.NotificationResponseDTO;
import br.com.ofisy.adapters.presenters.notification.NotificationPresenter;
import br.com.ofisy.application.notification.findall.FindAllNotificationsUseCase;
import br.com.ofisy.application.notification.findbyid.FindNotificationByIdUseCase;
import br.com.ofisy.application.notification.findunread.FindUnreadNotificationsUseCase;
import br.com.ofisy.application.notification.markasread.MarkNotificationAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final FindNotificationByIdUseCase findNotificationByIdUseCase;
    private final FindAllNotificationsUseCase findAllNotificationsUseCase;
    private final FindUnreadNotificationsUseCase findUnreadNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationPresenter.present(findNotificationByIdUseCase.execute(id)));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> findAll() {
        List<NotificationResponseDTO> list = findAllNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> findUnread() {
        List<NotificationResponseDTO> list = findUnreadNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID id) {
        MarkNotificationAsReadUseCase.MarkAsReadCommand cmd = new MarkNotificationAsReadUseCase.MarkAsReadCommand(id);
        return ResponseEntity.ok(NotificationPresenter.present(markNotificationAsReadUseCase.execute(cmd)));
    }
}
