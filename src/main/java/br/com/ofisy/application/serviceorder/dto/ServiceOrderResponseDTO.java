package br.com.ofisy.application.serviceorder.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderResponseDTO(
        UUID id,
        UUID vehicleId,
        UUID customerId,
        String report,
        String status,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime finishedAt,
        LocalDateTime updatedAt
) {
}
