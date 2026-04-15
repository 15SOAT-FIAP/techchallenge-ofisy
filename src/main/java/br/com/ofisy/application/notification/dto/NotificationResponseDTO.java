package br.com.ofisy.application.notification.dto;

import br.com.ofisy.domain.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        UUID stockId,
//        UUID serviceOrderId,
//        NotificationType type,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}