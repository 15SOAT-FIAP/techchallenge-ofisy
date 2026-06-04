package br.com.ofisy.application.notification.ports.out;

import br.com.ofisy.domain.notification.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPersistencePort {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findAll();
    List<Notification> findUnread();
}
