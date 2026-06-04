package br.com.ofisy.infrastructure.notification.persistence;

import br.com.ofisy.application.notification.ports.out.NotificationPersistencePort;
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
public class NotificationRepositoryAdapter implements NotificationPersistencePort, NotificationRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationEntityMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = mapper.toEntity(notification);
        if (jpaRepository.existsById(entity.getId())) {
            entity = entityManager.merge(entity);
        } else {
            entityManager.persist(entity);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Notification> findUnread() {
        return jpaRepository.findByRead(false).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Notification> findByRead(Boolean read) {
        return jpaRepository.findByRead(read).stream()
            .map(mapper::toDomain)
            .toList();
    }
}
