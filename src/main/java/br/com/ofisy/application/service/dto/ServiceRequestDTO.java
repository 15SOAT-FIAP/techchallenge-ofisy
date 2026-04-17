package br.com.ofisy.application.service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceRequestDTO(
        @NotNull(message = "ID do serviço do catálogo é obrigatório")
        UUID catalogServiceId,
        @NotNull(message = "Preço é obrigatório")
        BigDecimal price
) {}
