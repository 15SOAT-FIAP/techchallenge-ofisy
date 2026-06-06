package br.com.ofisy.adapters.controllers.vehicle.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponseDTO(
        UUID id,
        UUID customerId,
        String licensePlate,
        String model,
        String brand,
        String color,
        Integer year,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}