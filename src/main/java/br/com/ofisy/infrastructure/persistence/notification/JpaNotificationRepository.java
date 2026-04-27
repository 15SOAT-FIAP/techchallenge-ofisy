package br.com.ofisy.infrastructure.persistence.notification;

import br.com.ofisy.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRead(Boolean read);
}
