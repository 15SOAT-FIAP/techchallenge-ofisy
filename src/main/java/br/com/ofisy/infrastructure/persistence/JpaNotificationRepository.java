package br.com.ofisy.infrastructure.persistence;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepository {

    @Query("SELECT n FROM Notification n WHERE n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findAllUnread();

    List<Notification> findByStockId(UUID stockId);

//    List<Notification> findByServiceOrderId(UUID serviceOrderId);
}