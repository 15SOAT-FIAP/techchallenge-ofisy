package br.com.ofisy.application.notification.createlowstock;

import br.com.ofisy.application.notification.createlowstock.CreateLowStockNotificationUseCase.CreateLowStockCommand;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateLowStockNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private CreateLowStockNotificationService createLowStockService;

    @Test
    @DisplayName("Deve criar notificação de estoque baixo com sucesso")
    void shouldCreateLowStockNotification() {
        UUID stockId = UUID.randomUUID();
        CreateLowStockCommand command = new CreateLowStockCommand(stockId, "Amortecedor", 3, 5);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = createLowStockService.execute(command);

        assertThat(result.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(result.getStockId()).isEqualTo(stockId);
        assertThat(result.getMessage().getContent()).isEqualTo("Estoque baixo para Amortecedor. Quantidade atual: 3. Mínimo: 5");
        assertThat(result.isRead()).isFalse();

        verify(notificationRepository).save(any(Notification.class));
    }
}
