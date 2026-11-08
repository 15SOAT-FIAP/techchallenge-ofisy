package br.com.ofisy.adapters.controllers.quote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record StockItemRequestDTO(
        @NotNull(message = "ID do estoque é obrigatório")
        UUID stockId,

        @NotBlank(message = "Nome do produto é obrigatório")
        String productName,

        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço unitário deve ser maior que zero")
        BigDecimal unitPrice,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantity
) {}
