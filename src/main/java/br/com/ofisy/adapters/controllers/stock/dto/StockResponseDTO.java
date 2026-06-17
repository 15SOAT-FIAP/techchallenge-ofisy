package br.com.ofisy.adapters.controllers.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockResponseDTO(

        UUID id,

        String productName,

        String description,

        Integer quantity,

        BigDecimal unitPrice,

        String category,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        Integer minThreshold,

        Boolean isLowStock
) {}
