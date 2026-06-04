package br.com.ofisy.application.notification.services;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.ports.in.CreateLowStockCommand;
import br.com.ofisy.application.notification.ports.in.CreateQuoteCommand;
import br.com.ofisy.application.notification.ports.in.MarkAsReadCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;
import br.com.ofisy.application.notification.ports.out.NotificationPersistencePort;
import br.com.ofisy.application.notification.usecases.CreateLowStockNotificationUseCase;
import br.com.ofisy.application.notification.usecases.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.notification.usecases.FindAllNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.FindNotificationByIdUseCase;
import br.com.ofisy.application.notification.usecases.FindUnreadNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.MarkNotificationAsReadUseCase;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService 
    implements CreateLowStockNotificationUseCase,
               CreateQuoteNotificationUseCase,
               MarkNotificationAsReadUseCase,
               FindAllNotificationsUseCase,
               FindNotificationByIdUseCase,
               FindUnreadNotificationsUseCase {

    private final NotificationPersistencePort persistencePort;

    @Override
    @Transactional
    public NotificationResponse execute(CreateLowStockCommand command) {
        NotificationMessage message = NotificationMessage.forLowStock(
            command.productName(),
            command.currentQuantity(),
            command.minThreshold()
        );

        Notification notification = Notification.createForStock(
            command.stockId(),
            message
        );

        Notification saved = persistencePort.save(notification);
        return NotificationResponse.from(saved);
    }

    @Override
    @Transactional
    public NotificationResponse execute(CreateQuoteCommand command) {
        NotificationMessage message = NotificationMessage.forQuote(
            command.quoteId(),
            command.serviceOrderId(),
            command.totalPrice()
        );

        Notification notification = Notification.createForQuote(
            command.quoteId(),
            message
        );

        Notification saved = persistencePort.save(notification);
        return NotificationResponse.from(saved);
    }

    @Override
    @Transactional
    public NotificationResponse execute(MarkAsReadCommand command) {
        Notification notification = persistencePort.findById(command.notificationId())
            .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));

        notification.markAsRead();
        Notification saved = persistencePort.save(notification);
        return NotificationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> execute() {
        return persistencePort.findAll().stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> findUnread() {
        return persistencePort.findUnread().stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse execute(UUID id) {
        Notification notification = persistencePort.findById(id)
            .orElseThrow(() -> new NotificationNotFoundException(id));
        return NotificationResponse.from(notification);
    }
}
