package br.com.ofisy.application.customer.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerNotFoundExceptionTest {

    @Test
    void shouldContainIdInMessage() {
        var id = UUID.randomUUID();

        var exception = new CustomerNotFoundException(id);

        assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado com o ID: " + id);
    }

    @Test
    void shouldBeRuntimeException() {
        var exception = new CustomerNotFoundException(UUID.randomUUID());

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldHandleNullId() {
        var exception = new CustomerNotFoundException(null);

        assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado com o ID: null");
    }
}