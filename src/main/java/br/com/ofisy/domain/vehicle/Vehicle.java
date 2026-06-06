package br.com.ofisy.domain.vehicle;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Vehicle {

    private UUID id;
    private UUID customerId;
    private LicensePlate licensePlate;
    private String model;
    private String brand;
    private String color;
    private Integer year;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Vehicle(UUID customerId,
                   LicensePlate licensePlate,
                   String model,
                   String brand,
                   String color,
                   Integer year,
                   String description) {

        this.customerId = customerId;
        this.licensePlate = licensePlate;
        this.model = model;
        this.brand = brand;
        this.color = color;
        this.year = year;
        this.description = description;
    }

    public static Vehicle create(UUID customerId,
                                 LicensePlate licensePlate,
                                 String model,
                                 String brand,
                                 String color,
                                 Integer year,
                                 String description) {

        Vehicle vehicle = new Vehicle(customerId, licensePlate, model, brand, color, year, description);
        vehicle.createdAt = LocalDateTime.now();
        vehicle.updatedAt = LocalDateTime.now();
        return vehicle;
    }

    public static Vehicle reconstruct(UUID id,
                                      UUID customerId,
                                      LicensePlate licensePlate,
                                      String model,
                                      String brand,
                                      String color,
                                      Integer year,
                                      String description,
                                      LocalDateTime createdAt,
                                      LocalDateTime updatedAt) {

        Vehicle vehicle = new Vehicle(customerId, licensePlate, model, brand, color, year, description);
        vehicle.id = id;
        vehicle.createdAt = createdAt;
        vehicle.updatedAt = updatedAt;
        return vehicle;
    }
}
