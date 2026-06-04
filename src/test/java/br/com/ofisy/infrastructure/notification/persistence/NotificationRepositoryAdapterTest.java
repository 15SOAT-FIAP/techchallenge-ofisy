package br.com.ofisy.infrastructure.notification.persistence;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock
    private JpaNotificationRepository jpaRepository;

    @Mock
    private NotificationEntityMapper mapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private NotificationRepositoryAdapter repositoryAdapter;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Deve usar entityManager.merge quando a entidade já existe no banco")
        void shouldMergeWhenEntityExists() {
            Notification notification = mockNotification();
            NotificationJpaEntity entity = mockEntity(notification.getId());

            when(mapper.toEntity(notification)).thenReturn(entity);
            when(jpaRepository.existsById(entity.getId())).thenReturn(true);
            when(entityManager.merge(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(notification);

            Notification result = repositoryAdapter.save(notification);

            assertThat(result).isEqualTo(notification);
            verify(jpaRepository).existsById(entity.getId());
            verify(entityManager).merge(entity);
            verify(entityManager, never()).persist(any());
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("Deve usar entityManager.persist quando a entidade não existe no banco")
        void shouldPersistWhenEntityDoesNotExist() {
            Notification notification = mockNotification();
            NotificationJpaEntity entity = mockEntity(notification.getId());

            when(mapper.toEntity(notification)).thenReturn(entity);
            when(jpaRepository.existsById(entity.getId())).thenReturn(false);
            doNothing().when(entityManager).persist(entity);
            when(mapper.toDomain(entity)).thenReturn(notification);

            Notification result = repositoryAdapter.save(notification);

            assertThat(result).isEqualTo(notification);
            verify(jpaRepository).existsById(entity.getId());
            verify(entityManager).persist(entity);
            verify(entityManager, never()).merge(any());
            verify(mapper).toDomain(entity);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar notificação quando encontrada")
        void shouldReturnNotificationWhenFound() {
            UUID id = UUID.randomUUID();
            NotificationJpaEntity entity = mockEntity(id);
            Notification notification = mockNotification(id);

            when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(notification);

            Optional<Notification> result = repositoryAdapter.findById(id);

            assertThat(result).isPresent().contains(notification);
            verify(jpaRepository).findById(id);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não encontrada")
        void shouldReturnEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();

            when(jpaRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Notification> result = repositoryAdapter.findById(id);

            assertThat(result).isEmpty();
            verify(jpaRepository).findById(id);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de todas as notificações")
        void shouldReturnAllNotifications() {
            NotificationJpaEntity entity1 = mockEntity(UUID.randomUUID());
            NotificationJpaEntity entity2 = mockEntity(UUID.randomUUID());
            Notification notification1 = mockNotification(entity1.getId());
            Notification notification2 = mockNotification(entity2.getId());

            when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));
            when(mapper.toDomain(entity1)).thenReturn(notification1);
            when(mapper.toDomain(entity2)).thenReturn(notification2);

            List<Notification> result = repositoryAdapter.findAll();

            assertThat(result).hasSize(2).containsExactly(notification1, notification2);
            verify(jpaRepository).findAll();
        }
    }

    @Nested
    @DisplayName("findUnread")
    class FindUnread {

        @Test
        @DisplayName("Deve retornar apenas notificações não lidas")
        void shouldReturnUnreadNotifications() {
            NotificationJpaEntity entity = mockEntity(UUID.randomUUID());
            Notification notification = mockNotification(entity.getId());

            when(jpaRepository.findByRead(false)).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(notification);

            List<Notification> result = repositoryAdapter.findUnread();

            assertThat(result).hasSize(1).contains(notification);
            verify(jpaRepository).findByRead(false);
        }
    }

    @Nested
    @DisplayName("findByRead")
    class FindByRead {

        @Test
        @DisplayName("Deve retornar notificações filtradas pelo estado de leitura")
        void shouldReturnNotificationsFilteredByReadState() {
            NotificationJpaEntity entityTrue = mockEntity(UUID.randomUUID());
            Notification notificationTrue = mockNotification(entityTrue.getId());

            when(jpaRepository.findByRead(true)).thenReturn(List.of(entityTrue));
            when(mapper.toDomain(entityTrue)).thenReturn(notificationTrue);

            List<Notification> resultTrue = repositoryAdapter.findByRead(true);

            assertThat(resultTrue).hasSize(1).contains(notificationTrue);
            verify(jpaRepository).findByRead(true);

            NotificationJpaEntity entityFalse = mockEntity(UUID.randomUUID());
            Notification notificationFalse = mockNotification(entityFalse.getId());

            when(jpaRepository.findByRead(false)).thenReturn(List.of(entityFalse));
            when(mapper.toDomain(entityFalse)).thenReturn(notificationFalse);

            List<Notification> resultFalse = repositoryAdapter.findByRead(false);

            assertThat(resultFalse).hasSize(1).contains(notificationFalse);
            verify(jpaRepository).findByRead(false);
        }
    }

    private Notification mockNotification() {
        return mockNotification(UUID.randomUUID());
    }

    private Notification mockNotification(UUID id) {
        return Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(UUID.randomUUID())
            .message(NotificationMessage.fromString("Mock message"))
            .read(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private NotificationJpaEntity mockEntity(UUID id) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(id);
        entity.setType(NotificationType.LOW_STOCK);
        entity.setStockId(UUID.randomUUID());
        entity.setQuoteId(null);
        entity.setMessage("Mock message");
        entity.setRead(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
