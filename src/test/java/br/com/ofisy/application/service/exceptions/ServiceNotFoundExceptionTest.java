package br.com.ofisy.application.service.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        var id = "service-123";

        var exception = new ServiceNotFoundException(id);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).contains(id);
        assertThat(exception.getMessage()).contains("Serviço não encontrado");
    }

    @Test
    void shouldThrowExceptionWithCustomId() {
        var id = "test-service-id";

        assertThatThrownBy(() -> {
            throw new ServiceNotFoundException(id);
        }).isInstanceOf(ServiceNotFoundException.class)
                .hasMessageContaining("test-service-id")
                .hasMessageContaining("Serviço não encontrado com ID");
    }

    @Test
    void shouldMaintainExceptionMessage() {
        var id = "service-456";
        var exception = new ServiceNotFoundException(id);

        var message = exception.getMessage();

        assertThat(message).isEqualTo("Serviço não encontrado com ID: " + id);
    }

    @Test
    void shouldWorkWithDifferentIds() {
        var id1 = "service-1";
        var id2 = "service-2";

        var exception1 = new ServiceNotFoundException(id1);
        var exception2 = new ServiceNotFoundException(id2);

        assertThat(exception1.getMessage()).contains(id1);
        assertThat(exception2.getMessage()).contains(id2);
        assertThat(exception1.getMessage()).isNotEqualTo(exception2.getMessage());
    }

    @Test
    void shouldWorkWithNullId() {
        var id = (String) null;

        var exception = new ServiceNotFoundException(id);

        assertThat(exception.getMessage()).contains("null");
        assertThat(exception.getMessage()).contains("Serviço não encontrado com ID");
    }

    @Test
    void shouldWorkWithEmptyId() {
        var id = "";

        var exception = new ServiceNotFoundException(id);

        assertThat(exception.getMessage()).contains("Serviço não encontrado com ID: ");
    }

    @Test
    void shouldWorkWithSpecialCharacters() {
        var id = "service@#$%^&*()";

        var exception = new ServiceNotFoundException(id);

        assertThat(exception.getMessage()).contains(id);
    }

    @Test
    void shouldWorkWithLongId() {
        var id = "a".repeat(1000);

        var exception = new ServiceNotFoundException(id);

        assertThat(exception.getMessage()).contains(id);
    }
}

