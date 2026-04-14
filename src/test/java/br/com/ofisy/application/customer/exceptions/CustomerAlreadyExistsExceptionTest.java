package br.com.ofisy.application.customer.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerAlreadyExistsExceptionTest {

    @Test
    void shouldContainCpfCnpjInMessage() {
        var cpfCnpj = "52998224725";

        var exception = new CustomerAlreadyExistsException(cpfCnpj);

        assertThat(exception.getMessage()).isEqualTo("Cliente com CPF/CNPJ " + cpfCnpj + " já existe.");
    }

    @Test
    void shouldBeRuntimeException() {
        var exception = new CustomerAlreadyExistsException("52998224725");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldHandleNullCpfCnpj() {
        var exception = new CustomerAlreadyExistsException(null);

        assertThat(exception.getMessage()).isEqualTo("Cliente com CPF/CNPJ null já existe.");
    }
}