package br.com.ofisy.application.quote.dto;

import java.util.UUID;

public record QuoteServiceItemResponseDTO(
        UUID id,
        UUID serviceOrderExecutionId,
        java.math.BigDecimal price
) {}
