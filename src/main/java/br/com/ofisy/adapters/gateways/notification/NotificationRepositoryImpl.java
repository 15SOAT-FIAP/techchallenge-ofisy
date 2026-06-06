package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationMapper.toEntity(notification);
        if (entity.getId() != null && jpaRepository.existsById(entity.getId())) {
            entity = entityManager.merge(entity);
        } else {
            entityManager.persist(entity);
        }
        return NotificationMapper.toDomain(entity);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByRead(Boolean read) {
        return jpaRepository.findByRead(read).stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }
}
