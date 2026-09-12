package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByRead(Boolean read);

    List<NotificationEntity> findByType(NotificationType type);

    List<NotificationEntity> findByReadAndType(Boolean read, NotificationType type);
}
