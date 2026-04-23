package br.com.ofisy.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogServiceOrderServiceApplicationTest {

    private static final String VALID_NAME = "Oil Change";
    private static final String VALID_DESCRIPTION = "Professional oil change service";
    private static final BigDecimal VALID_PRICE = new BigDecimal("99.99");

    @Test
    void shouldCreateCatalogServiceWithValidData() {
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(catalogService).isNotNull();
        assertThat(catalogService.getName()).isEqualTo(VALID_NAME);
        assertThat(catalogService.getDescription()).isEqualTo(VALID_DESCRIPTION);
        assertThat(catalogService.getPrice()).isEqualTo(VALID_PRICE);
    }

    @Test
    void shouldNotHaveIdBeforePersistence() {
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(catalogService.getId()).isNull();
    }

    @Test
    void shouldSetCreatedAtOnCreation() {
        var before = LocalDateTime.now();
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var after = LocalDateTime.now();

        assertThat(catalogService.getCreatedAt()).isNotNull();
        assertThat(catalogService.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void shouldSetUpdatedAtOnCreation() {
        var before = LocalDateTime.now();
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var after = LocalDateTime.now();

        assertThat(catalogService.getUpdatedAt()).isNotNull();
        assertThat(catalogService.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void shouldSetCreatedAtEqualToUpdatedAtOnCreation() {
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(catalogService.getCreatedAt()).isEqualTo(catalogService.getUpdatedAt());
    }

    @Test
    void shouldCreateCatalogServiceWithDifferentPrices() {
        var prices = new BigDecimal[]{
                BigDecimal.ZERO,
                new BigDecimal("0.01"),
                new BigDecimal("50.00"),
                new BigDecimal("999.99"),
                new BigDecimal("9999.99")
        };

        for (BigDecimal price : prices) {
            var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, price);
            assertThat(catalogService.getPrice()).isEqualTo(price);
        }
    }

    @Test
    void shouldCreateCatalogServiceWithDifferentNames() {
        var names = new String[]{
                "Oil Change",
                "Tire Replacement",
                "Battery Service",
                "Engine Diagnostic"
        };

        for (String name : names) {
            var catalogService = Service.create(name, VALID_DESCRIPTION, VALID_PRICE);
            assertThat(catalogService.getName()).isEqualTo(name);
        }
    }

    @Test
    void shouldCreateCatalogServiceWithDifferentDescriptions() {
        var descriptions = new String[]{
                "Professional oil change service",
                "Replace all four tires with premium quality",
                "Battery diagnostic and replacement",
                "Complete engine diagnostic scan"
        };

        for (String description : descriptions) {
            var catalogService = Service.create(VALID_NAME, description, VALID_PRICE);
            assertThat(catalogService.getDescription()).isEqualTo(description);
        }
    }

    @Test
    void shouldCreateCatalogServiceWithNullName() {
        var catalogService = Service.create(null, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(catalogService.getName()).isNull();
    }

    @Test
    void shouldCreateCatalogServiceWithNullDescription() {
        var catalogService = Service.create(VALID_NAME, null, VALID_PRICE);

        assertThat(catalogService.getDescription()).isNull();
    }

    @Test
    void shouldCreateCatalogServiceWithNullPrice() {
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, null);

        assertThat(catalogService.getPrice()).isNull();
    }

    @Test
    void shouldCreateCatalogServiceWithEmptyStrings() {
        var catalogService = Service.create("", "", VALID_PRICE);

        assertThat(catalogService.getName()).isEmpty();
        assertThat(catalogService.getDescription()).isEmpty();
    }

    @Test
    void shouldCreateCatalogServiceWithSpecialCharacters() {
        var specialName = "Óleo & Filtro - Especial";
        var specialDescription = "Troca de óleo sintético (5W-40) + filtro de ar";

        var catalogService = Service.create(specialName, specialDescription, VALID_PRICE);

        assertThat(catalogService.getName()).isEqualTo(specialName);
        assertThat(catalogService.getDescription()).isEqualTo(specialDescription);
    }

    @Test
    void shouldCreateCatalogServiceWithLongStrings() {
        var longName = "A".repeat(255);
        var longDescription = "B".repeat(1000);

        var catalogService = Service.create(longName, longDescription, VALID_PRICE);

        assertThat(catalogService.getName()).isEqualTo(longName);
        assertThat(catalogService.getDescription()).isEqualTo(longDescription);
    }

    @Test
    void shouldCreateCatalogServiceWithNegativePrice() {
        var negativePrice = new BigDecimal("-50.00");
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, negativePrice);

        assertThat(catalogService.getPrice()).isEqualTo(negativePrice);
    }

    @Test
    void shouldCreateCatalogServiceWithVeryLargePrice() {
        var largePrice = new BigDecimal("999999999.99");
        var catalogService = Service.create(VALID_NAME, VALID_DESCRIPTION, largePrice);

        assertThat(catalogService.getPrice()).isEqualTo(largePrice);
    }

    @Test
    void shouldCreateMultipleIndependentInstances() {
        var catalogService1 = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
        var catalogService2 = Service.create(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);

        assertThat(catalogService1).isNotSameAs(catalogService2);
        assertThat(catalogService1.getName()).isEqualTo(catalogService2.getName());
        assertThat(catalogService1.getPrice()).isEqualTo(catalogService2.getPrice());
    }
}

