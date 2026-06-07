package br.com.ofisy.adapters.controllers.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        String type,
        UUID stockId,
        UUID quoteId,
        String message,
        Boolean read,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
