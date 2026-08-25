package br.com.ofisy.application.notification.findunreadstock;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUnreadStockNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindUnreadStockNotificationsService findUnreadStockService;

    @Test
    @DisplayName("Deve listar notificações de estoque não lidas com sucesso")
    void shouldFindUnreadStockNotifications() {
        Notification n = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        when(notificationRepository.findByReadAndType(false, NotificationType.LOW_STOCK)).thenReturn(List.of(n));

        List<Notification> result = findUnreadStockService.execute();

        assertThat(result).hasSize(1).containsExactly(n);
        verify(notificationRepository).findByReadAndType(false, NotificationType.LOW_STOCK);
    }
}
