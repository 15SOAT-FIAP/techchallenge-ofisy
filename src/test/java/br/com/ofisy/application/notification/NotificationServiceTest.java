package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MockNotificationSender mockNotificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private Notification buildNotification(UUID stockId, String message) {
        return Notification.createStockAlert(stockId, message);
    }

    private Stock buildStock(String productName, int quantity, int minThreshold) {
        return Stock.create(productName, "desc", quantity, BigDecimal.TEN, "cat", minThreshold);
    }

    // -------------------------------------------------------------------------
    // listAll()
    // -------------------------------------------------------------------------

    @Nested
    class ListAll {

        @Test
        void shouldReturnEmptyListWhenNoNotificationsExist() {
            when(notificationRepository.findAll()).thenReturn(List.of());

            List<NotificationResponseDTO> result = notificationService.listAll();

            assertThat(result).isEmpty();
            verify(notificationRepository).findAll();
        }

        @Test
        void shouldReturnMappedDTOsForAllNotifications() {
            UUID stockId = UUID.randomUUID();
            Notification n1 = buildNotification(stockId, "Alerta 1");
            Notification n2 = buildNotification(stockId, "Alerta 2");
            when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

            List<NotificationResponseDTO> result = notificationService.listAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).message()).isEqualTo("Alerta 1");
            assertThat(result.get(1).message()).isEqualTo("Alerta 2");
            assertThat(result).allSatisfy(dto -> {
                assertThat(dto.stockId()).isEqualTo(stockId);
                assertThat(dto.read()).isFalse();
                assertThat(dto.createdAt()).isNotNull();
            });
        }
    }

    // -------------------------------------------------------------------------
    // send()
    // -------------------------------------------------------------------------

    @Nested
    class Send {

        @Test
        void shouldPersistAndReturnDTOWhenStockExists() {
            UUID stockId = UUID.randomUUID();
            Stock stock = buildStock("Produto A", 5, 3);
            Notification saved = buildNotification(stockId, "Mensagem teste");

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            NotificationRequestDTO request = new NotificationRequestDTO(stockId, "Mensagem teste");
            NotificationResponseDTO result = notificationService.send(request);

            assertThat(result.message()).isEqualTo("Mensagem teste");
            assertThat(result.stockId()).isEqualTo(stockId);
            assertThat(result.read()).isFalse();
            verify(notificationRepository).save(any(Notification.class));
            verify(mockNotificationSender).send("Mensagem teste");
        }

        @Test
        void shouldThrowStockNotFoundExceptionWhenStockDoesNotExist() {
            UUID stockId = UUID.randomUUID();
            when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

            NotificationRequestDTO request = new NotificationRequestDTO(stockId, "Mensagem");

            assertThatThrownBy(() -> notificationService.send(request))
                    .isInstanceOf(StockNotFoundException.class)
                    .hasMessageContaining(stockId.toString());

            verify(notificationRepository, never()).save(any());
            verify(mockNotificationSender, never()).send(any());
        }
    }

    // -------------------------------------------------------------------------
    // markAsRead()
    // -------------------------------------------------------------------------

    @Nested
    class MarkAsRead {

        @Test
        void shouldMarkNotificationAsReadAndReturnUpdatedDTO() {
            UUID id = UUID.randomUUID();
            UUID stockId = UUID.randomUUID();
            Notification notification = buildNotification(stockId, "Alerta");
            when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(notification)).thenReturn(notification);

            NotificationResponseDTO result = notificationService.markAsRead(id);

            assertThat(result.read()).isTrue();
            verify(notificationRepository).findById(id);
            verify(notificationRepository).save(notification);
        }

        @Test
        void shouldThrowNotificationNotFoundExceptionWhenNotificationDoesNotExist() {
            UUID id = UUID.randomUUID();
            when(notificationRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(id))
                    .isInstanceOf(NotificationNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(notificationRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // sendStockAlert()
    // -------------------------------------------------------------------------

    @Nested
    class SendStockAlert {

        @Test
        void shouldBuildCorrectMessageAndPersistNotification() {
            Stock stock = buildStock("Caneta Azul", 2, 5);
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            notificationService.sendStockAlert(stock);

            Notification saved = captor.getValue();
            assertThat(saved.getMessage())
                    .isEqualTo("Estoque baixo: Caneta Azul atingiu o limite mínimo de 5 unidades");
            assertThat(saved.isRead()).isFalse();
        }

        @Test
        void shouldCallMockSenderWithCorrectMessage() {
            Stock stock = buildStock("Papel A4", 1, 10);
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            notificationService.sendStockAlert(stock);

            verify(mockNotificationSender).send("Estoque baixo: Papel A4 atingiu o limite mínimo de 10 unidades");
        }

        @Test
        void shouldPersistNotificationBeforeCallingMockSender() {
            Stock stock = buildStock("Borracha", 0, 3);
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            notificationService.sendStockAlert(stock);

            var inOrder = inOrder(notificationRepository, mockNotificationSender);
            inOrder.verify(notificationRepository).save(any(Notification.class));
            inOrder.verify(mockNotificationSender).send(any());
        }
    }
}
