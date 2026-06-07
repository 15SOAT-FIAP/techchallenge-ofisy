package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplTest {

    @Mock
    private JpaNotificationRepository jpaRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private NotificationRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        @DisplayName("Deve persistir a entidade no banco caso ela não exista")
        void shouldPersistEntityWhenItDoesNotExist() {
            Notification notification = validNotification();
            when(jpaRepository.existsById(notification.getId())).thenReturn(false);

            repository.save(notification);

            verify(entityManager).persist(any(NotificationEntity.class));
            verify(entityManager, never()).merge(any(NotificationEntity.class));
        }

        @Test
        @DisplayName("Deve fazer merge da entidade no banco caso ela já exista")
        void shouldMergeEntityWhenItExists() {
            Notification notification = validNotification();
            when(jpaRepository.existsById(notification.getId())).thenReturn(true);
            NotificationEntity mappedEntity = NotificationMapper.toEntity(notification);
            when(entityManager.merge(any(NotificationEntity.class))).thenReturn(mappedEntity);

            repository.save(notification);

            verify(entityManager).merge(any(NotificationEntity.class));
            verify(entityManager, never()).persist(any(NotificationEntity.class));
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("Deve buscar e mapear a notificação por ID com sucesso")
        void shouldReturnNotificationWhenFound() {
            UUID id = UUID.randomUUID();
            NotificationEntity entity = validEntity(id);
            when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<Notification> result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            assertThat(result.get().getMessage().getContent()).isEqualTo("Teste");
        }

        @Test
        @DisplayName("Deve retornar vazio se a notificação não for encontrada")
        void shouldReturnEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(jpaRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Notification> result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("Deve buscar e mapear todas as notificações")
        void shouldReturnAllNotifications() {
            UUID id = UUID.randomUUID();
            NotificationEntity entity = validEntity(id);
            when(jpaRepository.findAll()).thenReturn(List.of(entity));

            List<Notification> result = repository.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(id);
        }
    }

    @Nested
    class FindByRead {

        @Test
        @DisplayName("Deve buscar notificações filtrando pelo status de leitura")
        void shouldReturnNotificationsByReadStatus() {
            UUID id = UUID.randomUUID();
            NotificationEntity entity = validEntity(id);
            when(jpaRepository.findByRead(false)).thenReturn(List.of(entity));

            List<Notification> result = repository.findByRead(false);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(id);
        }
    }

    private Notification validNotification() {
        return Notification.builder()
                .id(UUID.randomUUID())
                .type(NotificationType.LOW_STOCK)
                .stockId(UUID.randomUUID())
                .message(NotificationMessage.fromString("Teste"))
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private NotificationEntity validEntity(UUID id) {
        return NotificationEntity.builder()
                .id(id)
                .type(NotificationType.LOW_STOCK)
                .stockId(UUID.randomUUID())
                .message("Teste")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
