package br.com.ofisy.application.servicecatalog.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCatalogNotFoundExceptionTest {

    @Test
    void ofId_shouldBuildMessageWithIdLabel() {
        var id = "service-123";

        var exception = ServiceCatalogNotFoundException.ofId(id);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com ID: " + id);
    }

    @Test
    void ofName_shouldBuildMessageWithNameLabel() {
        var name = "Troca de Óleo";

        var exception = ServiceCatalogNotFoundException.ofName(name);

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com nome: " + name);
    }

    @Test
    void ofId_shouldBeThrowable() {
        var id = "test-service-id";

        assertThatThrownBy(() -> { throw ServiceCatalogNotFoundException.ofId(id); })
                .isInstanceOf(ServiceCatalogNotFoundException.class)
                .hasMessageContaining(id)
                .hasMessageContaining("Serviço não encontrado com ID");
    }

    @Test
    void ofName_shouldBeThrowable() {
        var name = "Alinhamento";

        assertThatThrownBy(() -> { throw ServiceCatalogNotFoundException.ofName(name); })
                .isInstanceOf(ServiceCatalogNotFoundException.class)
                .hasMessageContaining(name)
                .hasMessageContaining("Serviço não encontrado com nome");
    }

    @Test
    void factories_shouldProduceDifferentMessagesForSameValue() {
        var value = "abc";

        var byId = ServiceCatalogNotFoundException.ofId(value);
        var byName = ServiceCatalogNotFoundException.ofName(value);

        assertThat(byId.getMessage()).isNotEqualTo(byName.getMessage());
        assertThat(byId.getMessage()).contains("ID");
        assertThat(byName.getMessage()).contains("nome");
    }

    @Test
    void ofId_shouldHandleNull() {
        var exception = ServiceCatalogNotFoundException.ofId(null);

        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com ID: null");
    }

    @Test
    void ofName_shouldHandleNull() {
        var exception = ServiceCatalogNotFoundException.ofName(null);

        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com nome: null");
    }

    @Test
    void ofId_shouldHandleEmpty() {
        var exception = ServiceCatalogNotFoundException.ofId("");

        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com ID: ");
    }

    @Test
    void ofName_shouldHandleEmpty() {
        var exception = ServiceCatalogNotFoundException.ofName("");

        assertThat(exception.getMessage()).isEqualTo("Serviço não encontrado com nome: ");
    }

    @Test
    void ofId_shouldHandleSpecialCharacters() {
        var id = "service@#$%^&*()";

        var exception = ServiceCatalogNotFoundException.ofId(id);

        assertThat(exception.getMessage()).contains(id);
    }

    @Test
    void ofName_shouldHandleLongValues() {
        var name = "a".repeat(1000);

        var exception = ServiceCatalogNotFoundException.ofName(name);

        assertThat(exception.getMessage()).contains(name);
    }
}