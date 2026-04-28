package br.com.ofisy.application.serviceCatalog.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServiceCatalogRequestDTO(
        @NotNull(message = "O preço do serviço é obrigatório")
        BigDecimal price,
        @NotNull(message = "O nome do serviço é obrigatório")
        String name,
        @NotNull(message = "A descrição do serviço é obrigatória")
        String description
) {}
