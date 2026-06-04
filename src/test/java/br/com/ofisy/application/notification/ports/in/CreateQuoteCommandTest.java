package br.com.ofisy.application.notification.ports.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateQuoteCommandTest {

    @Test
    @DisplayName("Deve criar comando válido com sucesso")
    void shouldCreateValidCommand() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("150.00");

        CreateQuoteCommand command = new CreateQuoteCommand(quoteId, serviceOrderId, price);

        assertThat(command.quoteId()).isEqualTo(quoteId);
        assertThat(command.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(command.totalPrice()).isEqualTo(price);
    }

    @Test
    @DisplayName("Deve falhar quando quoteId for nulo")
    void shouldFailWhenQuoteIdIsNull() {
        UUID serviceOrderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("150.00");

        assertThatThrownBy(() -> new CreateQuoteCommand(null, serviceOrderId, price))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("quoteId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve falhar quando serviceOrderId for nulo")
    void shouldFailWhenServiceOrderIdIsNull() {
        UUID quoteId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("150.00");

        assertThatThrownBy(() -> new CreateQuoteCommand(quoteId, null, price))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("serviceOrderId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve falhar quando totalPrice for nulo")
    void shouldFailWhenTotalPriceIsNull() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        assertThatThrownBy(() -> new CreateQuoteCommand(quoteId, serviceOrderId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("totalPrice não pode ser nulo");
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("100");

        CreateQuoteCommand cmd1 = new CreateQuoteCommand(quoteId, serviceOrderId, price);
        CreateQuoteCommand cmd2 = new CreateQuoteCommand(quoteId, serviceOrderId, price);

        assertThat(cmd1)
            .isEqualTo(cmd1)
            .isEqualTo(cmd2)
            .hasSameHashCodeAs(cmd2)
            .isNotEqualTo(null)
            .isNotEqualTo("Not a command")
            .isNotEqualTo(new CreateQuoteCommand(UUID.randomUUID(), serviceOrderId, price))
            .isNotEqualTo(new CreateQuoteCommand(quoteId, UUID.randomUUID(), price))
            .isNotEqualTo(new CreateQuoteCommand(quoteId, serviceOrderId, new BigDecimal("200")));
    }
}
