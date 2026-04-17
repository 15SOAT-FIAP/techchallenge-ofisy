package br.com.ofisy.infrastructure.persistence.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpa;

    @Override
    public Notification save(Notification n) {
        return jpa.save(n);
    }

    @Override
    public List<Notification> findAll() {
        return jpa.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<Notification> findAllUnread() {
        return jpa.findAllUnread();
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Notification> findByStockId(UUID id) {
        return jpa.findByStockId(id);
    }
}
