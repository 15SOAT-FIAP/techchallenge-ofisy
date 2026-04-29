package br.com.ofisy.application.servicecatalog.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCatalogNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        var id = "service-123";

        var exception = new ServiceCatalogNotFoundException(id);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).contains(id);
        assertThat(exception.getMessage()).contains("Serviço não encontrado");
    }

    @Test
    void shouldThrowExceptionWithCustomId() {
        var id = "test-service-id";

        assertThatThrownBy(() -> {
            throw new ServiceCatalogNotFoundException(id);
        }).isInstanceOf(ServiceCatalogNotFoundException.class)
                .hasMessageContaining("test-service-id")
                .hasMessageContaining("Serviço não encontrado com ID");
    }

    @Test
    void shouldMaintainExceptionMessage() {
        var id = "service-456";
        var exception = new ServiceCatalogNotFoundException(id);

        var message = exception.getMessage();

        assertThat(message).isEqualTo("Serviço não encontrado com ID: " + id);
    }

    @Test
    void shouldWorkWithDifferentIds() {
        var id1 = "service-1";
        var id2 = "service-2";

        var exception1 = new ServiceCatalogNotFoundException(id1);
        var exception2 = new ServiceCatalogNotFoundException(id2);

        assertThat(exception1.getMessage()).contains(id1);
        assertThat(exception2.getMessage()).contains(id2);
        assertThat(exception1.getMessage()).isNotEqualTo(exception2.getMessage());
    }

    @Test
    void shouldWorkWithNullId() {
        var id = (String) null;

        var exception = new ServiceCatalogNotFoundException(id);

        assertThat(exception.getMessage()).contains("null");
        assertThat(exception.getMessage()).contains("Serviço não encontrado com ID");
    }

    @Test
    void shouldWorkWithEmptyId() {
        var id = "";

        var exception = new ServiceCatalogNotFoundException(id);

        assertThat(exception.getMessage()).contains("Serviço não encontrado com ID: ");
    }

    @Test
    void shouldWorkWithSpecialCharacters() {
        var id = "service@#$%^&*()";

        var exception = new ServiceCatalogNotFoundException(id);

        assertThat(exception.getMessage()).contains(id);
    }

    @Test
    void shouldWorkWithLongId() {
        var id = "a".repeat(1000);

        var exception = new ServiceCatalogNotFoundException(id);

        assertThat(exception.getMessage()).contains(id);
    }
}

