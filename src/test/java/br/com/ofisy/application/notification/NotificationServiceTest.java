package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID notificationId;
    private UUID stockId;
    private Notification notification;
    private Notification readNotification;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        stockId = UUID.randomUUID();
        notification = createNotification();
        readNotification = createReadNotification();
    }

    @Test
    @DisplayName("Deve criar notificação de estoque baixo via método específico com stockId")
    void shouldCreateLowStockNotificationSuccessfully() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Stock stock = Stock.create(
                "Radiador",
                "Radiador de água com reservatório",
                2,
                new java.math.BigDecimal("150.00"),
                "Arrefecimento",
                5
        );

        NotificationResponseDTO result = notificationService.createLowStockNotification(stock);

        assertThat(result.type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.stockId()).isEqualTo(stock.getId());
        assertThat(result.message()).contains("Radiador");
        assertThat(result.message()).contains("2");
        assertThat(result.message()).contains("5");
        assertThat(result.read()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve criar notificação de orçamento via método específico com stockId nulo")
    void shouldCreateQuoteNotificationSuccessfully() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponseDTO result = notificationService.createQuoteNotification(
                "123",
                "João Silva",
                1500.00
        );

        assertThat(result.type()).isEqualTo(NotificationType.QUOTE_GENERATED.name());
        assertThat(result.stockId()).isNull();
        assertThat(result.message()).contains("123");
        assertThat(result.message()).contains("João Silva");
        assertThat(result.message()).contains("1500.00");
        assertThat(result.read()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve listar todas as notificações")
    void shouldFindAllNotifications() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification, readNotification));

        List<NotificationResponseDTO> result = notificationService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.get(1).type()).isEqualTo(NotificationType.QUOTE_GENERATED.name());
        verify(notificationRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar notificações não lidas")
    void shouldFindUnreadNotifications() {
        when(notificationRepository.findByRead(false)).thenReturn(List.of(notification));

        List<NotificationResponseDTO> result = notificationService.findUnread();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.LOW_STOCK.name());
        assertThat(result.get(0).read()).isFalse();
        verify(notificationRepository).findByRead(false);
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void shouldMarkNotificationAsRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        NotificationResponseDTO result = notificationService.markAsRead(notificationId);

        assertTrue(result.read());
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao marcar notificação inexistente como lida")
    void shouldThrowExceptionWhenNotificationNotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.markAsRead(notificationId));
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há notificações não lidas")
    void shouldReturnEmptyListWhenNoUnreadNotifications() {
        when(notificationRepository.findByRead(false)).thenReturn(List.of());

        List<NotificationResponseDTO> result = notificationService.findUnread();

        assertThat(result).isEmpty();
        verify(notificationRepository).findByRead(false);
    }

    private Notification createNotification() {
        return Notification.create(
                NotificationType.LOW_STOCK,
                UUID.randomUUID(),
                "Estoque baixo para o produto Radiador"
        );
    }

    private Notification createReadNotification() {
        Notification notification = Notification.create(
                NotificationType.QUOTE_GENERATED,
                null,
                "Orçamento #123 gerado"
        );
        notification.markAsRead();
        return notification;
    }
}
