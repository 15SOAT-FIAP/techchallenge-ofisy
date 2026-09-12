package br.com.ofisy.application.notification.markstockasread;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.markstockasread.MarkStockNotificationAsReadUseCase.MarkAsReadCommand;
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
class MarkStockNotificationAsReadServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MarkStockNotificationAsReadService markStockAsReadService;

    @Test
    @DisplayName("Deve marcar notificação de estoque como lida com sucesso")
    void shouldMarkStockNotificationAsRead() {
        UUID id = UUID.randomUUID();
        Notification n = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        assertThat(n.isRead()).isFalse();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = markStockAsReadService.execute(new MarkAsReadCommand(id));

        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findById(id);
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("Deve lançar erro ao marcar como lida uma notificação de estoque inexistente")
    void shouldThrowWhenNotificationNotFound() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markStockAsReadService.execute(new MarkAsReadCommand(id)))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao marcar como lida uma notificação que não é de estoque")
    void shouldThrowWhenNotificationIsWrongType() {
        UUID id = UUID.randomUUID();
        Notification quoteNotification = Notification.createForQuote(UUID.randomUUID(), NotificationMessage.fromString("Orçamento gerado"));
        when(notificationRepository.findById(id)).thenReturn(Optional.of(quoteNotification));

        assertThatThrownBy(() -> markStockAsReadService.execute(new MarkAsReadCommand(id)))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
        verify(notificationRepository, never()).save(any());
    }
}
