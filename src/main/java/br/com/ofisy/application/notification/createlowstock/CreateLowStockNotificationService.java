package br.com.ofisy.application.notification.createlowstock;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateLowStockNotificationService implements CreateLowStockNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public CreateLowStockNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification execute(CreateLowStockCommand command) {
        NotificationMessage message = NotificationMessage.forLowStock(
                command.productName(),
                command.currentQuantity(),
                command.minThreshold()
        );
        Notification notification = Notification.createForStock(command.stockId(), message);
        return notificationRepository.save(notification);
    }
}
