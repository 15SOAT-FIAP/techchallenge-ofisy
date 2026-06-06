package br.com.ofisy.application.notification.createquote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase.CreateQuoteCommand;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateQuoteNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private CreateQuoteNotificationService createQuoteService;

    @Test
    @DisplayName("Deve criar notificação de orçamento gerado com sucesso")
    void shouldCreateQuoteNotification() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("850.50");
        CreateQuoteCommand command = new CreateQuoteCommand(quoteId, serviceOrderId, price);

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = createQuoteService.execute(command);

        assertThat(result.getType()).isEqualTo(NotificationType.QUOTE_GENERATED);
        assertThat(result.getQuoteId()).isEqualTo(quoteId);
        assertThat(result.getMessage().getContent()).contains("Orçamento #").contains("Valor total: R$ 850.50");
        assertThat(result.isRead()).isFalse();

        verify(notificationRepository).save(any(Notification.class));
    }
}
