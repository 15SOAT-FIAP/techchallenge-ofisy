package br.com.ofisy.application.notification.findallstock;

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
class FindAllStockNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindAllStockNotificationsService findAllStockService;

    @Test
    @DisplayName("Deve listar todas as notificações de estoque com sucesso")
    void shouldFindAllStockNotifications() {
        Notification n = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        when(notificationRepository.findAllByType(NotificationType.LOW_STOCK)).thenReturn(List.of(n));

        List<Notification> result = findAllStockService.execute();

        assertThat(result).hasSize(1).containsExactly(n);
        verify(notificationRepository).findAllByType(NotificationType.LOW_STOCK);
    }
}
