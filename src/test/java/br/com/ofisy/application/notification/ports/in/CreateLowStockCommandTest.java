package br.com.ofisy.application.notification.ports.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateLowStockCommandTest {

    @Test
    @DisplayName("Deve criar comando válido com sucesso")
    void shouldCreateValidCommand() {
        UUID stockId = UUID.randomUUID();
        CreateLowStockCommand command = new CreateLowStockCommand(stockId, "Radiador", 2, 5);

        assertThat(command.stockId()).isEqualTo(stockId);
        assertThat(command.productName()).isEqualTo("Radiador");
        assertThat(command.currentQuantity()).isEqualTo(2);
        assertThat(command.minThreshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve falhar quando stockId for nulo")
    void shouldFailWhenStockIdIsNull() {
        assertThatThrownBy(() -> new CreateLowStockCommand(null, "Radiador", 2, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("stockId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve falhar quando productName for inválido")
    void shouldFailWhenProductNameIsInvalid() {
        UUID stockId = UUID.randomUUID();

        assertThatThrownBy(() -> new CreateLowStockCommand(stockId, null, 2, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("productName inválido");

        assertThatThrownBy(() -> new CreateLowStockCommand(stockId, "", 2, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("productName inválido");

        assertThatThrownBy(() -> new CreateLowStockCommand(stockId, "   ", 2, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("productName inválido");
    }

    @Test
    @DisplayName("Deve falhar quando quantidade for menor que zero")
    void shouldFailWhenQuantityIsNegative() {
        UUID stockId = UUID.randomUUID();

        assertThatThrownBy(() -> new CreateLowStockCommand(stockId, "Radiador", -1, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("currentQuantity inválido");
    }

    @Test
    @DisplayName("Deve falhar quando limite mínimo for menor que zero")
    void shouldFailWhenMinThresholdIsNegative() {
        UUID stockId = UUID.randomUUID();

        assertThatThrownBy(() -> new CreateLowStockCommand(stockId, "Radiador", 2, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("minThreshold inválido");
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        UUID stockId = UUID.randomUUID();

        CreateLowStockCommand cmd1 = new CreateLowStockCommand(stockId, "Pneu", 5, 10);
        CreateLowStockCommand cmd2 = new CreateLowStockCommand(stockId, "Pneu", 5, 10);

        assertThat(cmd1)
            .isEqualTo(cmd1)
            .isEqualTo(cmd2)
            .hasSameHashCodeAs(cmd2)
            .isNotEqualTo(null)
            .isNotEqualTo("Not a command")
            .isNotEqualTo(new CreateLowStockCommand(UUID.randomUUID(), "Pneu", 5, 10))
            .isNotEqualTo(new CreateLowStockCommand(stockId, "Outro", 5, 10))
            .isNotEqualTo(new CreateLowStockCommand(stockId, "Pneu", 4, 10))
            .isNotEqualTo(new CreateLowStockCommand(stockId, "Pneu", 5, 9));
    }
}
