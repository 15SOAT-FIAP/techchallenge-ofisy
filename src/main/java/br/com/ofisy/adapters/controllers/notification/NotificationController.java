package br.com.ofisy.adapters.controllers.notification;

import br.com.ofisy.adapters.controllers.notification.dto.NotificationResponseDTO;
import br.com.ofisy.adapters.presenters.notification.NotificationPresenter;
import br.com.ofisy.application.notification.findallserviceorder.FindAllServiceOrderNotificationsUseCase;
import br.com.ofisy.application.notification.findallstock.FindAllStockNotificationsUseCase;
import br.com.ofisy.application.notification.findserviceorderbyid.FindServiceOrderNotificationByIdUseCase;
import br.com.ofisy.application.notification.findstockbyid.FindStockNotificationByIdUseCase;
import br.com.ofisy.application.notification.findunreadserviceorder.FindUnreadServiceOrderNotificationsUseCase;
import br.com.ofisy.application.notification.findunreadstock.FindUnreadStockNotificationsUseCase;
import br.com.ofisy.application.notification.markserviceorderasread.MarkServiceOrderNotificationAsReadUseCase;
import br.com.ofisy.application.notification.markstockasread.MarkStockNotificationAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final FindStockNotificationByIdUseCase findStockNotificationByIdUseCase;
    private final FindServiceOrderNotificationByIdUseCase findServiceOrderNotificationByIdUseCase;
    private final FindAllStockNotificationsUseCase findAllStockNotificationsUseCase;
    private final FindUnreadStockNotificationsUseCase findUnreadStockNotificationsUseCase;
    private final FindAllServiceOrderNotificationsUseCase findAllServiceOrderNotificationsUseCase;
    private final FindUnreadServiceOrderNotificationsUseCase findUnreadServiceOrderNotificationsUseCase;
    private final MarkStockNotificationAsReadUseCase markStockNotificationAsReadUseCase;
    private final MarkServiceOrderNotificationAsReadUseCase markServiceOrderNotificationAsReadUseCase;

    @Override
    @GetMapping("/stock/{id}")
    @PreAuthorize("hasRole('STOCKMAN')")
    public ResponseEntity<NotificationResponseDTO> findStockById(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationPresenter.present(findStockNotificationByIdUseCase.execute(id)));
    }

    @Override
    @GetMapping("/service-orders/{id}")
    public ResponseEntity<NotificationResponseDTO> findServiceOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationPresenter.present(findServiceOrderNotificationByIdUseCase.execute(id)));
    }

    @Override
    @GetMapping("/stock")
    @PreAuthorize("hasRole('STOCKMAN')")
    public ResponseEntity<List<NotificationResponseDTO>> findAllStock() {
        List<NotificationResponseDTO> list = findAllStockNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/stock/unread")
    @PreAuthorize("hasRole('STOCKMAN')")
    public ResponseEntity<List<NotificationResponseDTO>> findUnreadStock() {
        List<NotificationResponseDTO> list = findUnreadStockNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/service-orders")
    public ResponseEntity<List<NotificationResponseDTO>> findAllServiceOrders() {
        List<NotificationResponseDTO> list = findAllServiceOrderNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/service-orders/unread")
    public ResponseEntity<List<NotificationResponseDTO>> findUnreadServiceOrders() {
        List<NotificationResponseDTO> list = findUnreadServiceOrderNotificationsUseCase.execute().stream()
                .map(NotificationPresenter::present)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    @PatchMapping("/stock/{id}/read")
    @PreAuthorize("hasRole('STOCKMAN')")
    public ResponseEntity<NotificationResponseDTO> markStockAsRead(@PathVariable UUID id) {
        MarkStockNotificationAsReadUseCase.MarkAsReadCommand cmd = new MarkStockNotificationAsReadUseCase.MarkAsReadCommand(id);
        return ResponseEntity.ok(NotificationPresenter.present(markStockNotificationAsReadUseCase.execute(cmd)));
    }

    @Override
    @PatchMapping("/service-orders/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markServiceOrderAsRead(@PathVariable UUID id) {
        MarkServiceOrderNotificationAsReadUseCase.MarkAsReadCommand cmd = new MarkServiceOrderNotificationAsReadUseCase.MarkAsReadCommand(id);
        return ResponseEntity.ok(NotificationPresenter.present(markServiceOrderNotificationAsReadUseCase.execute(cmd)));
    }
}
