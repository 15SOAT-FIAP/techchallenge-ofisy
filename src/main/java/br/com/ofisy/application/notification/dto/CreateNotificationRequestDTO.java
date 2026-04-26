package br.com.ofisy.application.notification.dto;

import br.com.ofisy.domain.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequestDTO(
        @NotNull NotificationType type,
        UUID stockId,
        @NotBlank @Size(max = 255) String message
) {}
