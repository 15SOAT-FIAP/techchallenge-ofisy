package br.com.ofisy.application.notification.findallserviceorder;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindAllServiceOrderNotificationsService implements FindAllServiceOrderNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public FindAllServiceOrderNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> execute() {
        return notificationRepository.findAllByType(NotificationType.QUOTE_GENERATED);
    }
}
