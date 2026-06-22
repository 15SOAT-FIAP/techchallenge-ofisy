package br.com.ofisy.adapters.controllers.quote.dto;

import br.com.ofisy.domain.quote.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuoteResponseDTO(
        UUID id,
        UUID serviceOrderId,
        QuoteStatus status,
        BigDecimal totalPrice,
        String quoteRefusalReason,
        List<QuoteStockItemResponseDTO> stockItems,
        List<QuoteServiceItemResponseDTO> serviceItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
