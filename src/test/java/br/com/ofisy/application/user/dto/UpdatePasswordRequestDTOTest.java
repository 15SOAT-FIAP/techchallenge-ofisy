package br.com.ofisy.application.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatePasswordRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTOWithNoViolations() {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("senha123", "novaSenha123");

        Set<ConstraintViolation<UpdatePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando senha atual está em branco")
    void shouldViolateWhenCurrentPasswordIsBlank() {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("", "novaSenha123");

        Set<ConstraintViolation<UpdatePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
    }

    @Test
    @DisplayName("Deve lançar violação quando nova senha está em branco")
    void shouldViolateWhenNewPasswordIsBlank() {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("senha123", "");

        Set<ConstraintViolation<UpdatePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    @DisplayName("Deve lançar violação quando nova senha tem menos de 8 caracteres")
    void shouldViolateWhenNewPasswordIsTooShort() {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("senha123", "123");

        Set<ConstraintViolation<UpdatePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }
}