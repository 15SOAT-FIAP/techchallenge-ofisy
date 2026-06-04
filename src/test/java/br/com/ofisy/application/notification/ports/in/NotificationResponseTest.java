package br.com.ofisy.application.notification.ports.in;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationResponseTest {

    @Test
    @DisplayName("Deve mapear de Notification para NotificationResponse com sucesso")
    void shouldMapFromDomainToResponse() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.fromString("Estoque baixo");
        LocalDateTime now = LocalDateTime.now();

        Notification domain = Notification.builder()
            .id(id)
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .message(message)
            .read(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        NotificationResponse response = NotificationResponse.from(domain);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.type()).isEqualTo("LOW_STOCK");
        assertThat(response.stockId()).isEqualTo(stockId);
        assertThat(response.quoteId()).isNull();
        assertThat(response.message()).isEqualTo("Estoque baixo");
        assertThat(response.read()).isFalse();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        String message = "Mensagem";
        LocalDateTime now = LocalDateTime.now();

        NotificationResponse r1 = new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, message, false, now, now);
        NotificationResponse r2 = new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, message, false, now, now);

        assertThat(r1)
            .isEqualTo(r1)
            .isEqualTo(r2)
            .hasSameHashCodeAs(r2)
            .isNotEqualTo(null)
            .isNotEqualTo("Not a response")
            .isNotEqualTo(new NotificationResponse(UUID.randomUUID(), "LOW_STOCK", stockId, quoteId, message, false, now, now))
            .isNotEqualTo(new NotificationResponse(id, "QUOTE_GENERATED", stockId, quoteId, message, false, now, now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", UUID.randomUUID(), quoteId, message, false, now, now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", stockId, UUID.randomUUID(), message, false, now, now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, "Outra", false, now, now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, message, true, now, now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, message, false, now.plusDays(1), now))
            .isNotEqualTo(new NotificationResponse(id, "LOW_STOCK", stockId, quoteId, message, false, now, now.plusDays(1)));
    }

    @Test
    @DisplayName("Deve testar o método toString")
    void testToString() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        LocalDateTime now = LocalDateTime.of(2026, 6, 4, 10, 0);

        NotificationResponse response = new NotificationResponse(id, "LOW_STOCK", null, null, "Mensagem", false, now, now);

        String result = response.toString();
        assertThat(result)
            .contains("NotificationResponse{")
            .contains("id=00000000-0000-0000-0000-000000000001")
            .contains("type=LOW_STOCK")
            .contains("message='Mensagem'")
            .contains("read=false");
    }
}
