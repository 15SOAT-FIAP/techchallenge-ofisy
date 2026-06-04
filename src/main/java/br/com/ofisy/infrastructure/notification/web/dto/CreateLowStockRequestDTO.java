package br.com.ofisy.infrastructure.notification.web.dto;

import java.util.UUID;

public record CreateLowStockRequestDTO(
    UUID stockId,
    String productName,
    int currentQuantity,
    int minThreshold
) {}
