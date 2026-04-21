package br.com.ofisy.domain.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "license_plate", nullable = false, unique = true))
    private LicensePlate licensePlate;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer year;

    @Column(columnDefinition = "TEXT")
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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Vehicle create(UUID customerId,
                                 LicensePlate licensePlate,
                                 String model,
                                 String brand,
                                 String color,
                                 Integer year,
                                 String description) {

        return new Vehicle(customerId, licensePlate, model, brand, color, year, description);
    }
}
