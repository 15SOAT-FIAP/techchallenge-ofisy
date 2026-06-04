package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.ports.in.CreateLowStockCommand;
import br.com.ofisy.application.notification.ports.in.CreateQuoteCommand;
import br.com.ofisy.application.notification.ports.in.MarkAsReadCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;
import br.com.ofisy.application.notification.ports.out.NotificationPersistencePort;
import br.com.ofisy.application.notification.services.NotificationApplicationService;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @InjectMocks
    private NotificationApplicationService notificationService;

    private UUID notificationId;
    private Notification stockNotification;
    private Notification quoteNotification;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        stockNotification = Notification.createForStock(
            UUID.randomUUID(),
            NotificationMessage.forLowStock("Radiador", 2, 5)
        );
        quoteNotification = Notification.createForQuote(
            UUID.randomUUID(),
            NotificationMessage.forQuote(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1500.00"))
        );
    }

    @Test
    @DisplayName("Deve criar notificação de estoque baixo com sucesso")
    void shouldCreateLowStockNotificationSuccessfully() {
        when(persistencePort.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateLowStockCommand command = new CreateLowStockCommand(
            UUID.randomUUID(), "Radiador", 2, 5
        );

        NotificationResponse result = notificationService.execute(command);

        assertThat(result.type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.message()).contains("Radiador");
        assertThat(result.message()).contains("2");
        assertThat(result.message()).contains("5");
        assertThat(result.read()).isFalse();
        verify(persistencePort).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve criar notificação de orçamento com sucesso")
    void shouldCreateQuoteNotificationSuccessfully() {
        when(persistencePort.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        CreateQuoteCommand command = new CreateQuoteCommand(quoteId, serviceOrderId, new BigDecimal("1500.00"));

        NotificationResponse result = notificationService.execute(command);

        assertThat(result.type()).isEqualTo(NotificationType.QUOTE_GENERATED.name());
        assertThat(result.quoteId()).isEqualTo(quoteId);
        assertThat(result.message()).contains(quoteId.toString());
        assertThat(result.message()).contains("1500.00");
        assertThat(result.read()).isFalse();
        verify(persistencePort).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve listar todas as notificações")
    void shouldFindAllNotifications() {
        when(persistencePort.findAll()).thenReturn(List.of(stockNotification, quoteNotification));

        List<NotificationResponse> result = notificationService.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.get(1).type()).isEqualTo(NotificationType.QUOTE_GENERATED.name());
        verify(persistencePort).findAll();
    }

    @Test
    @DisplayName("Deve listar notificações não lidas")
    void shouldFindUnreadNotifications() {
        when(persistencePort.findUnread()).thenReturn(List.of(stockNotification));

        List<NotificationResponse> result = notificationService.findUnread();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.get(0).read()).isFalse();
        verify(persistencePort).findUnread();
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void shouldMarkNotificationAsRead() {
        when(persistencePort.findById(notificationId)).thenReturn(Optional.of(stockNotification));
        when(persistencePort.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse result = notificationService.execute(new MarkAsReadCommand(notificationId));

        assertThat(result.read()).isTrue();
        verify(persistencePort).findById(notificationId);
        verify(persistencePort).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao marcar notificação inexistente como lida")
    void shouldThrowExceptionWhenNotificationNotFound() {
        when(persistencePort.findById(notificationId)).thenReturn(Optional.empty());

        MarkAsReadCommand command = new MarkAsReadCommand(notificationId);
        assertThrows(NotificationNotFoundException.class,
            () -> notificationService.execute(command));
        verify(persistencePort).findById(notificationId);
        verify(persistencePort, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há notificações não lidas")
    void shouldReturnEmptyListWhenNoUnreadNotifications() {
        when(persistencePort.findUnread()).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.findUnread();

        assertThat(result).isEmpty();
        verify(persistencePort).findUnread();
    }
}
