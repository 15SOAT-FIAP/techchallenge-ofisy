package br.com.ofisy.application.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationRequestDTO(
        @NotNull UUID stockId,
        @NotBlank String message
) {}
