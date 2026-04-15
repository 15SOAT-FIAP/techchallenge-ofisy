package br.com.ofisy.domain.user;

import br.com.ofisy.domain.user.exceptions.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @ParameterizedTest
    @CsvSource({
            "joao@ofisy.com, joao@ofisy.com",
            "JOAO@OFISY.COM, joao@ofisy.com",
            "  joao@ofisy.com  , joao@ofisy.com"
    })
    @DisplayName("Deve normalizar e criar emails válidos")
    void shouldNormalizeAndCreateValidEmails(String input, String expected) {
        Email email = new Email(input);
        assertThat(email.emailAddress()).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("Deve lançar exceção para emails inválidos")
    @ValueSource(strings = {"emailinvalido", "email@", "@dominio.com", "email@dominio", ""})
    void shouldThrowExceptionForInvalidEmails(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("E-mail informado é inválido!");
    }

    @Test
    @DisplayName("Deve lançar exceção para email nulo")
    void shouldThrowExceptionForNullEmail() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("E-mail informado é nulo!");
    }

    @Test
    @DisplayName("Deve validar email corretamente")
    void shouldValidateEmailCorrectly() {
        assertThat(Email.isValid("joao@ofisy.com")).isTrue();
        assertThat(Email.isValid("emailinvalido")).isFalse();
    }

}