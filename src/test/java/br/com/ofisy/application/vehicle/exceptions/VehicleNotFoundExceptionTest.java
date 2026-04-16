package br.com.ofisy.application.vehicle.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleNotFoundExceptionTest {

    @Test
    void shouldBeARuntimeException() {
        var exception = new VehicleNotFoundException(UUID.randomUUID());

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldContainIdInMessage() {
        var id = UUID.randomUUID();

        var exception = new VehicleNotFoundException(id);

        assertThat(exception.getMessage()).contains(id.toString());
    }

    @Test
    void shouldContainNotFoundKeywordInMessage() {
        var exception = new VehicleNotFoundException(UUID.randomUUID());

        assertThat(exception.getMessage()).contains("não encontrado");
    }
}
