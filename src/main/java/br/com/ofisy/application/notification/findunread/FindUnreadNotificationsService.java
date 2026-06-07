package br.com.ofisy.application.notification.findunread;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FindUnreadNotificationsService implements FindUnreadNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public FindUnreadNotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> execute() {
        return notificationRepository.findByRead(false);
    }
}
