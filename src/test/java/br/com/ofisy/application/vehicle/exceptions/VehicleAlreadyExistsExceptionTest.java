package br.com.ofisy.application.vehicle.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleAlreadyExistsExceptionTest {

    @Test
    void shouldBeARuntimeException() {
        var exception = new VehicleAlreadyExistsException("ABC1234");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldContainLicensePlateInMessage() {
        var licensePlate = "ABC1234";

        var exception = new VehicleAlreadyExistsException(licensePlate);

        assertThat(exception.getMessage()).contains(licensePlate);
    }

    @Test
    void shouldContainRegisteredKeywordInMessage() {
        var exception = new VehicleAlreadyExistsException("ABC1234");

        assertThat(exception.getMessage()).contains("já está registrado");
    }
}
