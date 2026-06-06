package br.com.ofisy.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationMessageTest {

    @Test
    @DisplayName("Deve validar NotificationMessage com erro de nulo ou vazio")
    void shouldValidateNotificationMessageContent() {
        assertThatThrownBy(() -> NotificationMessage.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensagem não pode ser vazia");

        assertThatThrownBy(() -> NotificationMessage.fromString("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensagem não pode ser vazia");
    }

    @Test
    @DisplayName("Deve formatar NotificationMessage para estoque baixo")
    void shouldFormatLowStockMessage() {
        NotificationMessage msg = NotificationMessage.forLowStock("Radiador", 5, 10);
        assertThat(msg.getContent()).isEqualTo("Estoque baixo para Radiador. Quantidade atual: 5. Mínimo: 10");
    }

    @Test
    @DisplayName("Deve formatar NotificationMessage para orçamento")
    void shouldFormatQuoteMessage() {
        UUID quoteId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("123.45");

        NotificationMessage msg = NotificationMessage.forQuote(quoteId, orderId, price);
        assertThat(msg.getContent()).contains("Orçamento #").contains("Valor total: R$ 123.45");
    }

    @Test
    @DisplayName("Deve lançar erro ao criar mensagem com mais de 255 caracteres")
    void shouldThrowWhenMessageLengthIsGreaterThan255() {
        String longMessage = "a".repeat(256);
        assertThatThrownBy(() -> NotificationMessage.fromString(longMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mensagem não pode ter mais de 255 caracteres");
    }
}
