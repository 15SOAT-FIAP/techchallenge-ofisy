package br.com.ofisy.application.service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceRequestDTO(
        @NotNull(message = "O preço do serviço é obrigatório")
        BigDecimal price,
        @NotNull(message = "O nome do serviço é obrigatório")
        String name,
        @NotNull(message = "A descrição do serviço é obrigatória")
        String description
) {}
