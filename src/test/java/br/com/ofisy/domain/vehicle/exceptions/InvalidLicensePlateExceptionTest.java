package br.com.ofisy.domain.vehicle.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidLicensePlateExceptionTest {

    @Test
    void shouldBeARuntimeException() {
        var exception = new InvalidLicensePlateException("INVALID");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldContainInvalidValueInMessage() {
        var invalidValue = "INVALID";

        var exception = new InvalidLicensePlateException(invalidValue);

        assertThat(exception.getMessage()).contains(invalidValue);
    }

    @Test
    void shouldContainInvalidKeywordInMessage() {
        var exception = new InvalidLicensePlateException("XPTO");

        assertThat(exception.getMessage()).contains("inválida");
    }
}
