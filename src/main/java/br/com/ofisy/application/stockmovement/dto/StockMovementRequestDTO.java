package br.com.ofisy.application.stockmovement.dto;

import br.com.ofisy.domain.stockmovement.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record StockMovementRequestDTO(

        @NotNull(message = "Estoque é obrigatório")
        UUID stockId,

        @NotNull(message = "Tipo é obrigatório")
        MovementType type,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantity,

        @NotNull(message = "Quantidade anterior é obrigatória")
        @Min(value = 0, message = "Quantidade anterior não pode ser negativa")
        Integer previousQuantity,

        @NotNull(message = "Quantidade nova é obrigatória")
        @Min(value = 0, message = "Quantidade nova não pode ser negativa")
        Integer newQuantity
) {}
