package br.com.ofisy.adapters.controllers.quote;

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
