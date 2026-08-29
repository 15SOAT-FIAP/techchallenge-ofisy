package br.com.ofisy.application.notification.findallstock;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindAllStockNotificationsService implements FindAllStockNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public FindAllStockNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> execute() {
        return notificationRepository.findAllByType(NotificationType.LOW_STOCK);
    }
}
