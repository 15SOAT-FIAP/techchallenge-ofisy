package br.com.ofisy.domain.servicecatalog;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogTest {

    @Test
    void create_shouldCreateServiceWithCorrectFields() {
        String name = "Oil Change";
        String description = "Change engine oil";
        BigDecimal price = new BigDecimal("50.00");

        ServiceCatalog serviceCatalog = br.com.ofisy.domain.servicecatalog.ServiceCatalog.create(name, description, price);

        assertNotNull(serviceCatalog);
        assertEquals(name, serviceCatalog.getName());
        assertEquals(description, serviceCatalog.getDescription());
        assertEquals(price, serviceCatalog.getPrice());
        assertNotNull(serviceCatalog.getCreatedAt());
        assertNotNull(serviceCatalog.getUpdatedAt());
    }

    @Test
    void create_shouldSetCreatedAtAndUpdatedAtToNow() {
        String name = "Brake Repair";
        String description = "Repair brakes";
        BigDecimal price = new BigDecimal("100.00");

        ServiceCatalog serviceCatalog = br.com.ofisy.domain.servicecatalog.ServiceCatalog.create(name, description, price);

        assertNotNull(serviceCatalog.getCreatedAt());
        assertNotNull(serviceCatalog.getUpdatedAt());
        assertTrue(serviceCatalog.getCreatedAt().isEqual(serviceCatalog.getUpdatedAt()) ||
                   serviceCatalog.getCreatedAt().isBefore(serviceCatalog.getUpdatedAt().plusNanos(1000000))); // allow small difference
    }
}
