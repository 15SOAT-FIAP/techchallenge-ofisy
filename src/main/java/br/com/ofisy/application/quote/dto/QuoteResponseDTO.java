package br.com.ofisy.application.quote.dto;

import java.util.List;
import java.util.UUID;

public record QuoteResponseDTO(
        UUID id,
        UUID serviceOrderId,
        br.com.ofisy.domain.quote.QuoteStatus status,
        java.math.BigDecimal totalPrice,
        String quoteRefusalReason,
        List<QuoteStockItemResponseDTO> stockItems,
        List<QuoteServiceItemResponseDTO> serviceItems,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
) {}
