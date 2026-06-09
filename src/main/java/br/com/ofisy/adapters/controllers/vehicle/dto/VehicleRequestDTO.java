package br.com.ofisy.adapters.controllers.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VehicleRequestDTO(
        @NotNull
        UUID customerId,
        @NotBlank
        String licensePlate,
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotBlank
        String color,
        @NotNull
        Integer year,
        String description
) {
}