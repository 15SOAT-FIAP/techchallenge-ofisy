package br.com.ofisy.application.quote.dto;

import java.util.UUID;

public record QuoteStockItemResponseDTO(
        UUID id,
        UUID stockId,
        String productName,
        java.math.BigDecimal unitPrice,
        Integer quantity,
        java.math.BigDecimal subtotal
) {}
