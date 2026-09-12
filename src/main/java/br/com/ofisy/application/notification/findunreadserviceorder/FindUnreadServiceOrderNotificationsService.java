package br.com.ofisy.application.notification.findunreadserviceorder;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindUnreadServiceOrderNotificationsService implements FindUnreadServiceOrderNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public FindUnreadServiceOrderNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> execute() {
        return notificationRepository.findByReadAndType(false, NotificationType.QUOTE_GENERATED);
    }
}
