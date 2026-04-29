package br.com.ofisy.application.servicecatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServiceCatalogRequestDTO(
        @NotNull(message = "O preço do serviço é obrigatório")
        BigDecimal price,
        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,
        @NotBlank(message = "A descrição do serviço é obrigatória")
        String description
) {}
