package br.com.ofisy.domain.notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findAllUnread();

    List<Notification> findByStockId(UUID stockId);

//    List<Notification> findByServiceOrderId(UUID serviceOrderId);
}