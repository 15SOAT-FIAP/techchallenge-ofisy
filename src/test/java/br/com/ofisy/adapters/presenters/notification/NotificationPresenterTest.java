package br.com.ofisy.adapters.presenters.notification;

import br.com.ofisy.adapters.controllers.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPresenterTest {

    @Test
    @DisplayName("Deve retornar null se a notificação for nula")
    void shouldReturnNullWhenNotificationIsNull() {
        assertThat(NotificationPresenter.present(null)).isNull();
    }

    @Test
    @DisplayName("Deve mapear todos os campos corretamente ao apresentar uma notificação")
    void shouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Notification notification = Notification.builder()
                .id(id)
                .type(NotificationType.LOW_STOCK)
                .stockId(stockId)
                .message(NotificationMessage.fromString("Estoque baixo"))
                .read(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        NotificationResponseDTO dto = NotificationPresenter.present(notification);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.type()).isEqualTo("LOW_STOCK");
        assertThat(dto.stockId()).isEqualTo(stockId);
        assertThat(dto.quoteId()).isNull();
        assertThat(dto.message()).isEqualTo("Estoque baixo");
        assertThat(dto.read()).isFalse();
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }
}
