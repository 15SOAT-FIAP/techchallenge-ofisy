package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();

    List<Notification> findByRead(Boolean read);

    List<Notification> findAllByType(NotificationType type);

    List<Notification> findByReadAndType(Boolean read, NotificationType type);
}
