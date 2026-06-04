package br.com.ofisy.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("Deve criar notificação de estoque baixo com sucesso")
    void shouldCreateLowStockNotificationSuccessfully() {
        UUID stockId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.forLowStock("Radiador", 2, 5);

        Notification notification = Notification.createForStock(stockId, message);

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.getQuoteId()).isNull();
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar notificação de orçamento com sucesso")
    void shouldCreateQuoteNotificationSuccessfully() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.forQuote(quoteId, serviceOrderId, new java.math.BigDecimal("1500.00"));

        Notification notification = Notification.createForQuote(quoteId, message);

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getType()).isEqualTo(NotificationType.QUOTE_GENERATED);
        assertThat(notification.getStockId()).isNull();
        assertThat(notification.getQuoteId()).isEqualTo(quoteId);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void shouldMarkAsRead() {
        NotificationMessage message = NotificationMessage.fromString("Estoque baixo");
        Notification notification = Notification.createForStock(UUID.randomUUID(), message);

        assertThat(notification.isRead()).isFalse();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar updatedAt ao marcar como lida")
    void shouldUpdateUpdatedAtWhenMarkAsRead() {
        NotificationMessage message = NotificationMessage.fromString("Estoque baixo");
        Notification notification = Notification.createForStock(UUID.randomUUID(), message);
        var initialUpdatedAt = notification.getUpdatedAt();

        notification.markAsRead();

        assertThat(notification.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
    }

    @Test
    @DisplayName("Deve reconstituir notificação com reconstitute")
    void shouldReconstituteNotification() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.fromString("Estoque baixo");
        var now = java.time.LocalDateTime.now();

        Notification notification = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .message(message)
            .read(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.fromString("Mensagem");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        Notification n1 = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        Notification n2 = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        // Mesma referência, objetos idênticos e testes de desigualdade
        assertThat(n1)
            .isEqualTo(n1)
            .isEqualTo(n2)
            .hasSameHashCodeAs(n2)
            .isNotEqualTo(null)
            .isNotEqualTo("Not a notification");

        // Id diferente
        Notification diffId = Notification.builder()
            .id(UUID.randomUUID())
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffId);

        // Tipo diferente
        Notification diffType = Notification.builder()
            .id(id)
            .type(NotificationType.QUOTE_GENERATED)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffType);

        // StockId diferente
        Notification diffStock = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(UUID.randomUUID())
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffStock);

        // QuoteId diferente
        Notification diffQuote = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(UUID.randomUUID())
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffQuote);

        // Message diferente
        Notification diffMsg = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(NotificationMessage.fromString("Outra"))
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffMsg);

        // Read diferente
        Notification diffRead = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffRead);

        // CreatedAt diferente
        Notification diffCreated = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now.plusDays(1))
            .updatedAt(now)
            .build();
        assertThat(n1).isNotEqualTo(diffCreated);

        // UpdatedAt diferente
        Notification diffUpdated = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now.plusDays(1))
            .build();
        assertThat(n1).isNotEqualTo(diffUpdated);
    }

    @Test
    @DisplayName("Deve testar o método toString")
    void testToString() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        NotificationMessage message = NotificationMessage.fromString("Conteúdo");
        var now = java.time.LocalDateTime.of(2026, 6, 4, 10, 0);

        Notification notification = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        String result = notification.toString();
        assertThat(result)
            .contains("Notification{")
            .contains("id=00000000-0000-0000-0000-000000000001")
            .contains("type=LOW_STOCK")
            .contains("message=NotificationMessage{Conteúdo}")
            .contains("read=false");
    }
}
