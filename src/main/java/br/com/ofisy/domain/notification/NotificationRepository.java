package br.com.ofisy.domain.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();

    List<Notification> findByRead(Boolean read);
}
