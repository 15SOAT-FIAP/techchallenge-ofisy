package br.com.ofisy.application.user.dto;

import br.com.ofisy.domain.user.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModifyUserRoleRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTOWithNoViolations() {
        ModifyUserRoleRequestDTO dto = new ModifyUserRoleRequestDTO(Role.ADMIN);

        Set<ConstraintViolation<ModifyUserRoleRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando role é nula")
    void shouldViolateWhenRoleIsNull() {
        ModifyUserRoleRequestDTO dto = new ModifyUserRoleRequestDTO(null);

        Set<ConstraintViolation<ModifyUserRoleRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }
}