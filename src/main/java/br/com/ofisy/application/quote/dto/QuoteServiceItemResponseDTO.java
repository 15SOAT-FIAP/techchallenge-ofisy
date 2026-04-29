package br.com.ofisy.application.quote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuoteServiceItemResponseDTO(
        UUID id,
        UUID serviceOrderExecutionId,
        BigDecimal price,
        LocalDateTime createdAt
) {}
