package br.com.ofisy.application.quote.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockItemRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTO() {
        var dto = new StockItemRequestDTO(UUID.randomUUID(), 2);

        Set<ConstraintViolation<StockItemRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando stockId é nulo")
    void shouldViolateWhenStockIdIsNull() {
        var dto = new StockItemRequestDTO(null, 2);

        Set<ConstraintViolation<StockItemRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("stockId"));
    }

    @Test
    @DisplayName("Deve lançar violação quando quantity é zero")
    void shouldViolateWhenQuantityIsZero() {
        var dto = new StockItemRequestDTO(UUID.randomUUID(), 0);

        Set<ConstraintViolation<StockItemRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    @DisplayName("Deve lançar violação quando quantity é nulo")
    void shouldViolateWhenQuantityIsNull() {
        var dto = new StockItemRequestDTO(UUID.randomUUID(), null);

        Set<ConstraintViolation<StockItemRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }
}
