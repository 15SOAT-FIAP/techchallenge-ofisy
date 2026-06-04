package br.com.ofisy.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationMessageTest {

    @Test
    @DisplayName("Deve criar mensagem de estoque baixo corretamente")
    void shouldCreateLowStockMessage() {
        NotificationMessage message = NotificationMessage.forLowStock("Filtro de Óleo", 5, 10);
        assertThat(message.getContent()).isEqualTo("Estoque baixo para Filtro de Óleo. Quantidade atual: 5. Mínimo: 10");
    }

    @Test
    @DisplayName("Deve criar mensagem de orçamento gerado corretamente")
    void shouldCreateQuoteMessage() {
        UUID quoteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID serviceOrderId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        NotificationMessage message = NotificationMessage.forQuote(quoteId, serviceOrderId, new BigDecimal("250.50"));

        assertThat(message.getContent()).isEqualTo("Orçamento #00000000-0000-0000-0000-000000000001 gerado para a ordem de serviço '00000000-0000-0000-0000-000000000002'. Valor total: R$ 250.50");
    }

    @Test
    @DisplayName("Deve criar mensagem a partir de string")
    void shouldCreateFromString() {
        NotificationMessage message = NotificationMessage.fromString("Mensagem genérica");
        assertThat(message.getContent()).isEqualTo("Mensagem genérica");
    }

    @Test
    @DisplayName("Não deve permitir mensagem nula ou vazia")
    void shouldNotAllowNullOrBlankContent() {
        assertThatThrownBy(() -> NotificationMessage.fromString(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mensagem não pode ser vazia");

        assertThatThrownBy(() -> NotificationMessage.fromString(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mensagem não pode ser vazia");

        assertThatThrownBy(() -> NotificationMessage.fromString("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mensagem não pode ser vazia");
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        NotificationMessage msg1 = NotificationMessage.fromString("Teste");
        NotificationMessage msg2 = NotificationMessage.fromString("Teste");
        NotificationMessage msg3 = NotificationMessage.fromString("Diferente");

        // Equals and HashCode
        assertThat(msg1)
            .isEqualTo(msg1)
            .isEqualTo(msg2)
            .isNotEqualTo(msg3)
            .isNotEqualTo(null)
            .isNotEqualTo("Uma String qualquer")
            .hasSameHashCodeAs(msg2);

        assertThat(msg1.hashCode()).isNotEqualTo(msg3.hashCode());
    }

    @Test
    @DisplayName("Deve testar o método toString")
    void testToString() {
        NotificationMessage message = NotificationMessage.fromString("Conteúdo");
        assertThat(message).hasToString("NotificationMessage{Conteúdo}");
    }
}
