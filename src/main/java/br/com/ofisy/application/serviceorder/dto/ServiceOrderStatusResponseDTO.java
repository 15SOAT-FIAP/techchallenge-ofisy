package br.com.ofisy.application.serviceorder.dto;

import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderStatusResponseDTO(
        UUID id,
        UUID vehicleId,
        UUID customerId,
        ServiceOrderStatus status,
        LocalDateTime updatedAt
) {
}
