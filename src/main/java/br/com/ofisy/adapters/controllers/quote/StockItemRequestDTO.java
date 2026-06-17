package br.com.ofisy.adapters.controllers.quote;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockItemRequestDTO(
        @NotNull(message = "ID do estoque é obrigatório")
        UUID stockId,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantity
) {}
