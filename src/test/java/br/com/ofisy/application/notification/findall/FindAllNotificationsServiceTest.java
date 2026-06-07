package br.com.ofisy.application.notification.findall;

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
class FindAllNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindAllNotificationsService findAllService;

    @Test
    @DisplayName("Deve listar todas as notificações com sucesso")
    void shouldFindAllNotifications() {
        Notification n1 = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Mensagem 1"));
        Notification n2 = Notification.createForQuote(UUID.randomUUID(), NotificationMessage.fromString("Mensagem 2"));
        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        List<Notification> result = findAllService.execute();

        assertThat(result).hasSize(2).containsExactly(n1, n2);
        verify(notificationRepository).findAll();
    }
}
