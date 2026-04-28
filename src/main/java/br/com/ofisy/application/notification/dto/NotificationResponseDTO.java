package br.com.ofisy.application.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        String type,
        UUID stockId,
        String message,
        Boolean read,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
