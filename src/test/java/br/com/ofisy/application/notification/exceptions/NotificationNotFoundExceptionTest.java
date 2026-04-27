package br.com.ofisy.application.notification.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem correta")
    void shouldCreateExceptionWithCorrectMessage() {
        UUID id = UUID.randomUUID();

        NotificationNotFoundException exception = new NotificationNotFoundException(id);

        assertThat(exception.getMessage()).isEqualTo("Notificação não encontrada: " + id);
    }
}
