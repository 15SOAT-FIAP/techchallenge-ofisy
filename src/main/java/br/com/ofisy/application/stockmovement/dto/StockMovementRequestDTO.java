package br.com.ofisy.application.stockmovement.dto;

import br.com.ofisy.domain.stockmovement.MovementType;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record StockMovementRequestDTO(

        @NotBlank(message = "Estoque é obrigatório")
        UUID stockId,

        @NotBlank(message = "Tipo é obrigatório")
        MovementType type,

        @NotBlank(message = "Quantidade é obrigatório")
        Integer quantity,

        @NotBlank(message = "Quantidade anterior é obrigatório")
        Integer previousQuantity,

        @NotBlank(message = "Quantidade nova é obrigatório")
        Integer newQuantity
) {}
