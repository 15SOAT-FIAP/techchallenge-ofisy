package br.com.ofisy.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderServiceApplicationTest {

    private static final String VALID_NAME = "Oil Change";
    private static final String VALID_DESCRIPTION = "Professional oil change service";
    private static final BigDecimal VALID_PRICE = new BigDecimal("99.99");

    @Test
    void shouldCreateserviceWithValidData() {
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(service).isNotNull();
        assertThat(service.getName()).isEqualTo(VALID_NAME);
        assertThat(service.getDescription()).isEqualTo(VALID_DESCRIPTION);
        assertThat(service.getPrice()).isEqualTo(VALID_PRICE);
    }

    @Test
    void shouldNotHaveIdBeforePersistence() {
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(service.getId()).isNull();
    }

    @Test
    void shouldSetCreatedAtOnCreation() {
        var before = LocalDateTime.now();
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var after = LocalDateTime.now();

        assertThat(service.getCreatedAt()).isNotNull();
        assertThat(service.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void shouldSetUpdatedAtOnCreation() {
        var before = LocalDateTime.now();
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var after = LocalDateTime.now();

        assertThat(service.getUpdatedAt()).isNotNull();
        assertThat(service.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void shouldCreateServiceWithDifferentPrices() {
        var prices = new BigDecimal[]{
                BigDecimal.ZERO,
                new BigDecimal("0.01"),
                new BigDecimal("50.00"),
                new BigDecimal("999.99"),
                new BigDecimal("9999.99")
        };

        for (BigDecimal price : prices) {
            var service = Service.create(VALID_NAME, VALID_DESCRIPTION, price);
            assertThat(service.getPrice()).isEqualTo(price);
        }
    }

    @Test
    void shouldCreateserviceWithDifferentNames() {
        var names = new String[]{
                "Oil Change",
                "Tire Replacement",
                "Battery Service",
                "Engine Diagnostic"
        };

        for (String name : names) {
            var service = Service.create(name, VALID_DESCRIPTION, VALID_PRICE);
            assertThat(service.getName()).isEqualTo(name);
        }
    }

    @Test
    void shouldCreateserviceWithDifferentDescriptions() {
        var descriptions = new String[]{
                "Professional oil change service",
                "Replace all four tires with premium quality",
                "Battery diagnostic and replacement",
                "Complete engine diagnostic scan"
        };

        for (String description : descriptions) {
            var service = Service.create(VALID_NAME, description, VALID_PRICE);
            assertThat(service.getDescription()).isEqualTo(description);
        }
    }

    @Test
    void shouldCreateserviceWithNullName() {
        var service = Service.create(null, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(service.getName()).isNull();
    }

    @Test
    void shouldCreateserviceWithNullDescription() {
        var service = Service.create(VALID_NAME, null, VALID_PRICE);

        assertThat(service.getDescription()).isNull();
    }

    @Test
    void shouldCreateserviceWithNullPrice() {
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, null);

        assertThat(service.getPrice()).isNull();
    }

    @Test
    void shouldCreateserviceWithEmptyStrings() {
        var service = Service.create("", "", VALID_PRICE);

        assertThat(service.getName()).isEmpty();
        assertThat(service.getDescription()).isEmpty();
    }

    @Test
    void shouldCreateserviceWithSpecialCharacters() {
        var specialName = "Óleo & Filtro - Especial";
        var specialDescription = "Troca de óleo sintético (5W-40) + filtro de ar";

        var service = Service.create(specialName, specialDescription, VALID_PRICE);

        assertThat(service.getName()).isEqualTo(specialName);
        assertThat(service.getDescription()).isEqualTo(specialDescription);
    }

    @Test
    void shouldCreateserviceWithLongStrings() {
        var longName = "A".repeat(255);
        var longDescription = "B".repeat(1000);

        var service = Service.create(longName, longDescription, VALID_PRICE);

        assertThat(service.getName()).isEqualTo(longName);
        assertThat(service.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void shouldCreateserviceWithNegativePrice() {
        var negativePrice = new BigDecimal("-50.00");
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, negativePrice);

        assertThat(service.getPrice()).isEqualTo(negativePrice);
    }

    @Test
    void shouldCreateserviceWithVeryLargePrice() {
        var largePrice = new BigDecimal("999999999.99");
        var service = Service.create(VALID_NAME, VALID_DESCRIPTION, largePrice);

        assertThat(service.getPrice()).isEqualTo(largePrice);
    }

    @Test
    void shouldCreateMultipleIndependentInstances() {
        var service1 = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var service2 = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(service1).isNotSameAs(service2);
        assertThat(service1.getName()).isEqualTo(service2.getName());
        assertThat(service1.getPrice()).isEqualTo(service2.getPrice());
    }
}

