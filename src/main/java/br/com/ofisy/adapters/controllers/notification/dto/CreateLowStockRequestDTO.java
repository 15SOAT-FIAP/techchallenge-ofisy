package br.com.ofisy.adapters.controllers.notification.dto;

import java.util.UUID;

public record CreateLowStockRequestDTO(
        UUID stockId,
        String message
) {}
