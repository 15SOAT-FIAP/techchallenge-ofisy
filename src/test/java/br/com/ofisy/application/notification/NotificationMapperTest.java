package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationMapperTest {

    @Test
    @DisplayName("Deve converter Notification para DTO com sucesso")
    void shouldConvertNotificationToDTO() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        NotificationType type = NotificationType.LOW_STOCK;
        String message = "Estoque baixo para Radiador";

        Notification notification = Notification.createForStock(stockId, message);
        setIdViaReflection(notification, id);
        notification.markAsRead();

        NotificationResponseDTO dto = NotificationMapper.toDTO(notification);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.type()).isEqualTo(type.name());
        assertThat(dto.stockId()).isEqualTo(stockId);
        assertThat(dto.quoteId()).isNull();
        assertThat(dto.message()).isEqualTo(message);
        assertThat(dto.read()).isTrue();
        assertThat(dto.createdAt()).isNotNull();
        assertThat(dto.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve converter Notification sem stockId para DTO")
    void shouldConvertNotificationWithoutStockIdToDTO() {
        UUID id = UUID.randomUUID();
        NotificationType type = NotificationType.QUOTE_GENERATED;
        String message = "Orçamento #123 gerado";

        Notification notification = Notification.createForQuote(null, message);
        setIdViaReflection(notification, id);

        NotificationResponseDTO dto = NotificationMapper.toDTO(notification);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.type()).isEqualTo(type.name());
        assertThat(dto.stockId()).isNull();
        assertThat(dto.quoteId()).isNull();
        assertThat(dto.message()).isEqualTo(message);
        assertThat(dto.read()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção quando notification for nulo")
    void shouldThrowExceptionWhenNotificationIsNull() {
        assertThatThrownBy(() -> NotificationMapper.toDTO(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Notification não pode ser nulo");
    }

    private void setIdViaReflection(Notification notification, UUID id) {
        try {
            java.lang.reflect.Field idField = Notification.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(notification, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
