package br.com.ofisy.application.quote.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteServiceItemResponseDTO(
        UUID id,
        UUID serviceOrderExecutionId,
        BigDecimal price
) {}
