package br.com.ofisy.application.vehicle.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleRequestDTOTest {

    private static Validator validator;

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_PLATE = "ABC1234";
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldHaveNoViolationsWithValidData() {
        var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_PLATE, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

        var violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Nested
    class CustomerIdValidation {

        @Test
        void shouldHaveViolationWhenCustomerIdIsNull() {
            var dto = new VehicleRequestDTO(null, VALID_PLATE, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
        }
    }

    @Nested
    class LicensePlateValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenLicensePlateIsNullBlankOrEmpty(String plate) {
            var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, plate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }
    }

    @Nested
    class ModelValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenModelIsNullBlankOrEmpty(String model) {
            var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_PLATE, model, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("model"));
        }
    }

    @Nested
    class BrandValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenBrandIsNullBlankOrEmpty(String brand) {
            var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_PLATE, VALID_MODEL, brand, VALID_COLOR, VALID_YEAR, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("brand"));
        }
    }

    @Nested
    class ColorValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void shouldHaveViolationWhenColorIsNullBlankOrEmpty(String color) {
            var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_PLATE, VALID_MODEL, VALID_BRAND, color, VALID_YEAR, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("color"));
        }
    }

    @Nested
    class YearValidation {

        @Test
        void shouldHaveViolationWhenYearIsNull() {
            var dto = new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_PLATE, VALID_MODEL, VALID_BRAND, VALID_COLOR, null, null);

            var violations = validator.validate(dto);

            assertThat(violations).isNotEmpty()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("year"));
        }
    }

    @Nested
    class MultipleViolations {

        @Test
        void shouldHaveSixViolationsWhenAllRequiredFieldsAreNull() {
            var dto = new VehicleRequestDTO(null, null, null, null, null, null, null);

            var violations = validator.validate(dto);

            assertThat(violations).hasSize(6);
        }
    }
}
