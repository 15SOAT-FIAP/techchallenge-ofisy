package br.com.ofisy.application.notification.markasread;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkNotificationAsReadService implements MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkNotificationAsReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification execute(MarkAsReadCommand command) {
        Notification notification = notificationRepository.findById(command.id())
                .orElseThrow(() -> new NotificationNotFoundException(command.id()));
        notification.markAsRead();
        return notificationRepository.save(notification);
    }
}
