package br.com.ofisy.application.quote.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReproveQuoteRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTO() {
        var dto = new ReproveQuoteRequestDTO("Peças fora do orçamento");

        Set<ConstraintViolation<ReproveQuoteRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
