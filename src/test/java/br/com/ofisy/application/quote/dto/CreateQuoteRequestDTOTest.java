package br.com.ofisy.application.quote.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateQuoteRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Deve criar DTO válido sem violações")
    void shouldCreateValidDTO() {
        var dto = new CreateQuoteRequestDTO(
                UUID.randomUUID(),
                List.of(new StockItemRequestDTO(UUID.randomUUID(), 2)),
                List.of()
        );

        Set<ConstraintViolation<CreateQuoteRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar violação quando serviceOrderId é nulo")
    void shouldViolateWhenServiceOrderIdIsNull() {
        var dto = new CreateQuoteRequestDTO(
                null,
                List.of(new StockItemRequestDTO(UUID.randomUUID(), 2)),
                List.of()
        );

        Set<ConstraintViolation<CreateQuoteRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("serviceOrderId"));
    }
}
