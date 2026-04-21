package br.com.ofisy.application.stockmovement.dto;

import br.com.ofisy.domain.stockmovement.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponseDTO(

        UUID id,

        UUID stockId,

        MovementType movementType,

        Integer quantity,

        Integer previousQuantity,

        Integer newQuantity,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}
