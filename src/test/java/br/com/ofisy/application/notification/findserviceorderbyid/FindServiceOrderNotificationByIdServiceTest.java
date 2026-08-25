package br.com.ofisy.application.notification.findserviceorderbyid;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindServiceOrderNotificationByIdServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private FindServiceOrderNotificationByIdService findServiceOrderByIdService;

    @Test
    @DisplayName("Deve buscar notificação de ordem de serviço por id com sucesso")
    void shouldFindServiceOrderNotificationById() {
        UUID id = UUID.randomUUID();
        Notification n = Notification.createForQuote(UUID.randomUUID(), NotificationMessage.fromString("Orçamento gerado"));
        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));

        Notification result = findServiceOrderByIdService.execute(id);

        assertThat(result).isEqualTo(n);
        verify(notificationRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar notificação de ordem de serviço por id inexistente")
    void shouldThrowWhenNotificationNotFoundById() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findServiceOrderByIdService.execute(id))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar por id de uma notificação que não é de ordem de serviço")
    void shouldThrowWhenNotificationIsWrongType() {
        UUID id = UUID.randomUUID();
        Notification stockNotification = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        when(notificationRepository.findById(id)).thenReturn(Optional.of(stockNotification));

        assertThatThrownBy(() -> findServiceOrderByIdService.execute(id))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada");

        verify(notificationRepository).findById(id);
    }
}
