package br.com.ofisy.application.serviceOrderService.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderServiceNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        var id = UUID.randomUUID().toString();

        var exception = new ServiceOrderServiceNotFoundException(id);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).contains(id);
        assertThat(exception.getMessage()).contains("Serviço não encontrado");
    }

    @Test
    void shouldThrowExceptionWithCustomId() {
        var id = "test-id-123";

        assertThatThrownBy(() -> {
            throw new ServiceOrderServiceNotFoundException(id);
        }).isInstanceOf(ServiceOrderServiceNotFoundException.class)
                .hasMessageContaining("test-id-123")
                .hasMessageContaining("Serviço não encontrado com ID");
    }

    @Test
    void shouldMaintainExceptionMessage() {
        var id = UUID.randomUUID().toString();
        var exception = new ServiceOrderServiceNotFoundException(id);

        var message = exception.getMessage();

        assertThat(message).isEqualTo("Serviço não encontrado com ID: " + id);
    }

    @Test
    void shouldWorkWithDifferentIds() {
        var id1 = UUID.randomUUID().toString();
        var id2 = UUID.randomUUID().toString();

        var exception1 = new ServiceOrderServiceNotFoundException(id1);
        var exception2 = new ServiceOrderServiceNotFoundException(id2);

        assertThat(exception1.getMessage()).contains(id1);
        assertThat(exception2.getMessage()).contains(id2);
        assertThat(exception1.getMessage()).isNotEqualTo(exception2.getMessage());
    }
}

