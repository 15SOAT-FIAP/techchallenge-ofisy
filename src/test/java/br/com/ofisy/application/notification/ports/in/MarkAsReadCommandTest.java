package br.com.ofisy.application.notification.ports.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkAsReadCommandTest {

    @Test
    @DisplayName("Deve criar comando válido com sucesso")
    void shouldCreateValidCommand() {
        UUID id = UUID.randomUUID();
        MarkAsReadCommand command = new MarkAsReadCommand(id);
        assertThat(command.notificationId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve falhar quando notificationId for nulo")
    void shouldFailWhenNotificationIdIsNull() {
        assertThatThrownBy(() -> new MarkAsReadCommand(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("notificationId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve testar comportamento de igualdade (equals/hashCode)")
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();

        MarkAsReadCommand cmd1 = new MarkAsReadCommand(id);
        MarkAsReadCommand cmd2 = new MarkAsReadCommand(id);

        assertThat(cmd1)
            .isEqualTo(cmd1)
            .isEqualTo(cmd2)
            .hasSameHashCodeAs(cmd2)
            .isNotEqualTo(null)
            .isNotEqualTo("Not a command")
            .isNotEqualTo(new MarkAsReadCommand(UUID.randomUUID()));
    }
}
