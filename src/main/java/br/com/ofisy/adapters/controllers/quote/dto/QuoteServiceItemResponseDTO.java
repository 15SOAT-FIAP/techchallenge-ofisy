package br.com.ofisy.adapters.controllers.quote.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuoteServiceItemResponseDTO(
        UUID id,
        UUID serviceCatalogId,
        BigDecimal price,
        LocalDateTime createdAt
) {}
