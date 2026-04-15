package br.com.ofisy.application.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTOWithNoViolations() {
        LoginRequestDTO dto = new LoginRequestDTO("joao@ofisy.com", "senha123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando email está em branco")
    void shouldViolateWhenEmailIsBlank() {
        LoginRequestDTO dto = new LoginRequestDTO("", "senha123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Deve lançar violação quando senha está em branco")
    void shouldViolateWhenPasswordIsBlank() {
        LoginRequestDTO dto = new LoginRequestDTO("joao@ofisy.com", "");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}