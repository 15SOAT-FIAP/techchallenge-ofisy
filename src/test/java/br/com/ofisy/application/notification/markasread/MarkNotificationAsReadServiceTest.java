package br.com.ofisy.application.notification.markasread;

import br.com.ofisy.application.notification.markasread.MarkNotificationAsReadUseCase.MarkAsReadCommand;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MarkNotificationAsReadService markAsReadService;

    @Test
    @DisplayName("Deve marcar notificação como lida com sucesso")
    void shouldMarkNotificationAsRead() {
        UUID id = UUID.randomUUID();
        Notification n = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Mensagem"));
        assertThat(n.isRead()).isFalse();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = markAsReadService.execute(new MarkAsReadCommand(id));

        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findById(id);
        verify(notificationRepository).save(n);
    }
}
