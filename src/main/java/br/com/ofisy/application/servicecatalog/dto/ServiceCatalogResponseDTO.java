package br.com.ofisy.application.servicecatalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceCatalogResponseDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
