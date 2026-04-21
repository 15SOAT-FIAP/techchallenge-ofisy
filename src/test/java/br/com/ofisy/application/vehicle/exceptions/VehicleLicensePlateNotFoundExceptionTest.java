package br.com.ofisy.application.vehicle.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleLicensePlateNotFoundExceptionTest {

    @Test
    void shouldBeARuntimeException() {
        var exception = new VehicleLicensePlateNotFoundException("ABC1234");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldContainLicensePlateInMessage() {
        var licensePlate = "ABC1234";

        var exception = new VehicleLicensePlateNotFoundException(licensePlate);

        assertThat(exception.getMessage()).contains(licensePlate);
    }

    @Test
    void shouldContainNotFoundKeywordInMessage() {
        var exception = new VehicleLicensePlateNotFoundException("ABC1234");

        assertThat(exception.getMessage()).contains("não encontrado");
    }
}
