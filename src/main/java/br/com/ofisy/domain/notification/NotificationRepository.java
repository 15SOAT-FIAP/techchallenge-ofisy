package br.com.ofisy.domain.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findAll();

    List<Notification> findAllUnread();

    Optional<Notification> findById(UUID id);

    List<Notification> findByStockId(UUID stockId);

}