package br.com.ofisy.application.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequestDTO(
        @NotBlank @Size(max = 100) String type,
        UUID stockId,
        @NotBlank @Size(max = 500) String message
) {}
