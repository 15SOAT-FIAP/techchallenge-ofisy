package br.com.ofisy.adapters.controllers.user.dto;

import br.com.ofisy.domain.user.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTOWithNoViolations() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "João Silva", "joao@ofisy.com", "senha123", Role.ATTENDANT
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando nome está em branco")
    void shouldViolateWhenNameIsBlank() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "", "joao@ofisy.com", "senha123", Role.ATTENDANT
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("Deve lançar violação quando email está em branco")
    void shouldViolateWhenEmailIsBlank() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "João Silva", "", "senha123", Role.ATTENDANT
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Deve lançar violação quando email é inválido")
    void shouldViolateWhenEmailIsInvalid() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "João Silva", "emailinvalido", "senha123", Role.ATTENDANT
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Deve lançar violação quando senha tem menos de 8 caracteres")
    void shouldViolateWhenPasswordIsTooShort() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "João Silva", "joao@ofisy.com", "123", Role.ATTENDANT
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Deve lançar violação quando role é nula")
    void shouldViolateWhenRoleIsNull() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "João Silva", "joao@ofisy.com", "senha123", null
        );

        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }
}