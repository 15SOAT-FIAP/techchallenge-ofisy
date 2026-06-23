package br.com.ofisy.domain.servicecatalog;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ServiceCatalog {

    private UUID id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ServiceCatalog(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public static ServiceCatalog create(String name, String description, BigDecimal price) {
        ServiceCatalog service = new ServiceCatalog(name, description, price);
        service.createdAt = LocalDateTime.now();
        service.updatedAt = LocalDateTime.now();
        return service;
    }

    public static ServiceCatalog reconstruct(UUID id, String name, String description, BigDecimal price,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        ServiceCatalog service = new ServiceCatalog(name, description, price);
        service.id = id;
        service.createdAt = createdAt;
        service.updatedAt = updatedAt;
        return service;
    }
}
