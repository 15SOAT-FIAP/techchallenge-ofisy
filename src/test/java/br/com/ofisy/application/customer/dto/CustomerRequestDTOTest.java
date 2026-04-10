package br.com.ofisy.application.customer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRequestDTOTest {

    private static Validator validator;

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldHaveNoViolationsWithValidData() {
        var dto = new CustomerRequestDTO(VALID_CPF, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        var violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Nested
    class CpfCnpjValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenCpfCnpjIsNullBlankOrEmpty(String cpfCnpj) {
            var dto = new CustomerRequestDTO(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("CPF/CNPJ é obrigatório"));
        }
    }

    @Nested
    class NameValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenNameIsNullBlankOrEmpty(String name) {
            var dto = new CustomerRequestDTO(VALID_CPF, name, VALID_EMAIL, VALID_PHONE);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("Nome é obrigatório"));
        }
    }

    @Nested
    class EmailValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenEmailIsNullBlankOrEmpty(String email) {
            var dto = new CustomerRequestDTO(VALID_CPF, VALID_NAME, email, VALID_PHONE);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-an-email", "missing@", "@no-local", "spaces in@email.com"})
        void shouldHaveViolationWhenEmailIsInvalid(String invalidEmail) {
            var dto = new CustomerRequestDTO(VALID_CPF, VALID_NAME, invalidEmail, VALID_PHONE);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("Email deve ser válido"));
        }
    }

    @Nested
    class PhoneValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenPhoneIsNullBlankOrEmpty(String phone) {
            var dto = new CustomerRequestDTO(VALID_CPF, VALID_NAME, VALID_EMAIL, phone);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("Telefone é obrigatório"));
        }
    }

    @Nested
    class MultipleViolations {

        @Test
        void shouldHaveMultipleViolationsWhenAllFieldsAreNull() {
            var dto = new CustomerRequestDTO(null, null, null, null);

            var violations = validator.validate(dto);

            assertThat(violations).hasSize(4);
        }

        @Test
        void shouldHaveMultipleViolationsWhenAllFieldsAreEmpty() {
            var dto = new CustomerRequestDTO("", "", "", "");

            var violations = validator.validate(dto);

            assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
        }
    }
}