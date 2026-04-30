package br.com.ofisy.application.serviceorderexecution.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderExecutionNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        var id = UUID.randomUUID();

        var exception = new ServiceOrderExecutionNotFoundException(id);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).contains(id.toString());
        assertThat(exception.getMessage()).contains("Execução de serviço não encontrado");
    }

    @Test
    void shouldThrowExceptionWithCustomId() {
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> {
            throw new ServiceOrderExecutionNotFoundException(id);
        }).isInstanceOf(ServiceOrderExecutionNotFoundException.class)
                .hasMessageContaining(id.toString())
                .hasMessageContaining("Execução de serviço não encontrado com ID");
    }

    @Test
    void shouldMaintainExceptionMessage() {
        var id = UUID.randomUUID();
        var exception = new ServiceOrderExecutionNotFoundException(id);

        var message = exception.getMessage();

        assertThat(message).isEqualTo("Execução de serviço não encontrado com ID: " + id);
    }

    @Test
    void shouldWorkWithDifferentIds() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        var exception1 = new ServiceOrderExecutionNotFoundException(id1);
        var exception2 = new ServiceOrderExecutionNotFoundException(id2);

        assertThat(exception1.getMessage()).contains(id1.toString());
        assertThat(exception2.getMessage()).contains(id2.toString());
        assertThat(exception1.getMessage()).isNotEqualTo(exception2.getMessage());
    }
}

