package br.com.ofisy.application.customer.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerCpfCnpjNotFoundExceptionTest {

    @Test
    void shouldContainCpfCnpjInMessage() {
        var cpfCnpj = "52998224725";

        var exception = new CustomerCpfCnpjNotFoundException(cpfCnpj);

        assertThat(exception.getMessage()).isEqualTo("Cliente com CPF/CNPJ " + cpfCnpj + " não encontrado.");
    }

    @Test
    void shouldBeRuntimeException() {
        var exception = new CustomerCpfCnpjNotFoundException("52998224725");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldHandleNullAndEmptyCpfCnpj(String cpfCnpj) {
        var exception = new CustomerCpfCnpjNotFoundException(cpfCnpj);

        assertThat(exception.getMessage()).isEqualTo("Cliente com CPF/CNPJ " + cpfCnpj + " não encontrado.");
    }
}