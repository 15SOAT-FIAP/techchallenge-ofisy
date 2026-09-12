package br.com.ofisy.application.notification.markserviceorderasread;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.markserviceorderasread.MarkServiceOrderNotificationAsReadUseCase.MarkAsReadCommand;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkServiceOrderNotificationAsReadServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MarkServiceOrderNotificationAsReadService markServiceOrderAsReadService;

    @Test
    @DisplayName("Deve marcar notificação de ordem de serviço como lida com sucesso")
    void shouldMarkServiceOrderNotificationAsRead() {
        UUID id = UUID.randomUUID();
        Notification n = Notification.createForQuote(UUID.randomUUID(), NotificationMessage.fromString("Orçamento gerado"));
        assertThat(n.isRead()).isFalse();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = markServiceOrderAsReadService.execute(new MarkAsReadCommand(id));

        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findById(id);
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("Deve lançar erro ao marcar como lida uma notificação de ordem de serviço inexistente")
    void shouldThrowWhenNotificationNotFound() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markServiceOrderAsReadService.execute(new MarkAsReadCommand(id)))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao marcar como lida uma notificação que não é de ordem de serviço")
    void shouldThrowWhenNotificationIsWrongType() {
        UUID id = UUID.randomUUID();
        Notification stockNotification = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        when(notificationRepository.findById(id)).thenReturn(Optional.of(stockNotification));

        assertThatThrownBy(() -> markServiceOrderAsReadService.execute(new MarkAsReadCommand(id)))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).save(any());
    }
}
