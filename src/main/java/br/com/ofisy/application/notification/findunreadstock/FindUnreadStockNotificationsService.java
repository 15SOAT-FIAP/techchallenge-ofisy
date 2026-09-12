package br.com.ofisy.application.notification.findunreadstock;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindUnreadStockNotificationsService implements FindUnreadStockNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public FindUnreadStockNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> execute() {
        return notificationRepository.findByReadAndType(false, NotificationType.LOW_STOCK);
    }
}
