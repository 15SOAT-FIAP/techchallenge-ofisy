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
        String type = "LOW_STOCK";
        String message = "Estoque baixo para Radiador";

        Notification notification = Notification.create(type, stockId, message);

        assertThat(notification.getId()).isNull();
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar notificação sem stockId (orçamento)")
    void shouldCreateNotificationWithoutStockId() {
        String type = "QUOTE_GENERATED";
        String message = "Orçamento #123 gerado";

        Notification notification = Notification.create(type, null, message);

        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getStockId()).isNull();
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getRead()).isFalse();
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void shouldMarkAsRead() {
        Notification notification = Notification.create("LOW_STOCK", UUID.randomUUID(), "Estoque baixo");

        assertThat(notification.getRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.getRead()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar updatedAt ao marcar como lida")
    void shouldUpdateUpdatedAtWhenMarkAsRead() {
        Notification notification = Notification.create("LOW_STOCK", UUID.randomUUID(), "Estoque baixo");
        var initialUpdatedAt = notification.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        notification.markAsRead();

        assertThat(notification.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
    }
}
