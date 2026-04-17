package br.com.ofisy.infrastructure.persistence.notification;

import br.com.ofisy.domain.notification.Notification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplTest {

    @Mock
    private JpaNotificationRepository jpa;

    @InjectMocks
    private NotificationRepositoryImpl repository;

    private Notification notification() {
        return Notification.createStockAlert(UUID.randomUUID(), "Estoque baixo: produto X");
    }

    @Nested
    class Save {

        @Test
        void shouldDelegateToJpaAndReturnSavedNotification() {
            var n = notification();
            when(jpa.save(n)).thenReturn(n);

            var result = repository.save(n);

            assertThat(result).isSameAs(n);
            verify(jpa).save(n);
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldDelegateToJpaWithDescSort() {
            var sort = Sort.by(Sort.Direction.DESC, "createdAt");
            var list = List.of(notification(), notification());
            when(jpa.findAll(sort)).thenReturn(list);

            var result = repository.findAll();

            assertThat(result).isEqualTo(list);
            verify(jpa).findAll(sort);
        }

        @Test
        void shouldReturnEmptyListWhenNoNotifications() {
            var sort = Sort.by(Sort.Direction.DESC, "createdAt");
            when(jpa.findAll(sort)).thenReturn(List.of());

            var result = repository.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindAllUnread {

        @Test
        void shouldDelegateToJpaFindAllUnread() {
            var list = List.of(notification());
            when(jpa.findAllUnread()).thenReturn(list);

            var result = repository.findAllUnread();

            assertThat(result).isEqualTo(list);
            verify(jpa).findAllUnread();
        }

        @Test
        void shouldReturnEmptyListWhenNoUnreadNotifications() {
            when(jpa.findAllUnread()).thenReturn(List.of());

            var result = repository.findAllUnread();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnNotificationWhenFound() {
            var id = UUID.randomUUID();
            var n = notification();
            when(jpa.findById(id)).thenReturn(Optional.of(n));

            var result = repository.findById(id);

            assertThat(result).isPresent().contains(n);
            verify(jpa).findById(id);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByStockId {

        @Test
        void shouldDelegateToJpaFindByStockId() {
            var stockId = UUID.randomUUID();
            var list = List.of(notification());
            when(jpa.findByStockId(stockId)).thenReturn(list);

            var result = repository.findByStockId(stockId);

            assertThat(result).isEqualTo(list);
            verify(jpa).findByStockId(stockId);
        }

        @Test
        void shouldReturnEmptyListWhenNoNotificationsForStock() {
            var stockId = UUID.randomUUID();
            when(jpa.findByStockId(stockId)).thenReturn(List.of());

            var result = repository.findByStockId(stockId);

            assertThat(result).isEmpty();
        }
    }
}
