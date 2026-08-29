package br.com.ofisy.application.notification.findunreadserviceorder;

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
class FindUnreadServiceOrderNotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindUnreadServiceOrderNotificationsService findUnreadServiceOrderService;

    @Test
    @DisplayName("Deve listar notificações de ordem de serviço não lidas com sucesso")
    void shouldFindUnreadServiceOrderNotifications() {
        Notification n = Notification.createForQuote(UUID.randomUUID(), NotificationMessage.fromString("Orçamento gerado"));
        when(notificationRepository.findByReadAndType(false, NotificationType.QUOTE_GENERATED)).thenReturn(List.of(n));

        List<Notification> result = findUnreadServiceOrderService.execute();

        assertThat(result).hasSize(1).containsExactly(n);
        verify(notificationRepository).findByReadAndType(false, NotificationType.QUOTE_GENERATED);
    }
}
