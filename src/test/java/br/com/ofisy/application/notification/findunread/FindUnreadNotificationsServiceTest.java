package br.com.ofisy.application.notification.findunread;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationRepository;
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
class FindUnreadNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindUnreadNotificationsService findUnreadService;

    @Test
    @DisplayName("Deve listar notificações não lidas com sucesso")
    void shouldFindUnreadNotifications() {
        Notification n = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Mensagem"));
        when(notificationRepository.findByRead(false)).thenReturn(List.of(n));

        List<Notification> result = findUnreadService.execute();

        assertThat(result).hasSize(1).containsExactly(n);
        verify(notificationRepository).findByRead(false);
    }
}
