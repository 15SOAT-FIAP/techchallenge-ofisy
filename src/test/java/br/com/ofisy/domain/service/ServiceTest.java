package br.com.ofisy.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void create_shouldCreateServiceWithCorrectFields() {
        String name = "Oil Change";
        String description = "Change engine oil";
        BigDecimal price = new BigDecimal("50.00");

        Service service = Service.create(name, description, price);

        assertNotNull(service);
        assertEquals(name, service.getName());
        assertEquals(description, service.getDescription());
        assertEquals(price, service.getPrice());
        assertNotNull(service.getCreatedAt());
        assertNotNull(service.getUpdatedAt());
    }

    @Test
    void create_shouldSetCreatedAtAndUpdatedAtToNow() {
        String name = "Brake Repair";
        String description = "Repair brakes";
        BigDecimal price = new BigDecimal("100.00");

        Service service = Service.create(name, description, price);

        assertNotNull(service.getCreatedAt());
        assertNotNull(service.getUpdatedAt());
        assertTrue(service.getCreatedAt().isEqual(service.getUpdatedAt()) ||
                   service.getCreatedAt().isBefore(service.getUpdatedAt().plusNanos(1000000))); // allow small difference
    }
}
