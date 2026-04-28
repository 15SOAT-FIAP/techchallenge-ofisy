package br.com.ofisy.application.quote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuoteStockItemResponseDTO(
        UUID id,
        UUID stockId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        LocalDateTime createdAt
) {}
