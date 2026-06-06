package br.com.ofisy.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("Deve criar notificação com sucesso")
    void shouldCreateNotificationSuccessfully() {
        UUID stockId = UUID.randomUUID();
        NotificationType type = NotificationType.LOW_STOCK;
        String message = "Estoque baixo para Radiador";

        Notification notification = Notification.createForStock(stockId, NotificationMessage.fromString(message));

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.getMessage().getContent()).isEqualTo(message);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar notificação sem stockId (orçamento)")
    void shouldCreateNotificationWithoutStockId() {
        NotificationType type = NotificationType.QUOTE_GENERATED;
        String message = "Orçamento #123 gerado";

        Notification notification = Notification.createForQuote(null, NotificationMessage.fromString(message));

        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getStockId()).isNull();
        assertThat(notification.getMessage().getContent()).isEqualTo(message);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void shouldMarkAsRead() {
        Notification notification = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));

        assertThat(notification.isRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar updatedAt ao marcar como lida")
    void shouldUpdateUpdatedAtWhenMarkAsRead() {
        Notification notification = Notification.createForStock(UUID.randomUUID(), NotificationMessage.fromString("Estoque baixo"));
        var initialUpdatedAt = notification.getUpdatedAt();

        notification.markAsRead();

        assertThat(notification.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
    }

    @Test
    @DisplayName("Deve criar notificação usando Builder com sucesso")
    void shouldCreateNotificationUsingBuilder() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        NotificationMessage msg = NotificationMessage.fromString("Estoque crítico");

        Notification notification = Notification.builder()
                .id(id)
                .type(NotificationType.LOW_STOCK)
                .stockId(stockId)
                .message(msg)
                .read(true)
                .build();

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.getMessage()).isEqualTo(msg);
        assertThat(notification.isRead()).isTrue();
    }

}
